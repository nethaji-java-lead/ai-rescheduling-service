package com.carebridge.scheduling;

import com.carebridge.enums.ProviderStatus;
import com.carebridge.enums.SlotStatus;
import com.carebridge.enums.TriggerReason;
import com.carebridge.model.Appointment;
import com.carebridge.model.Provider;
import com.carebridge.model.Slot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slotmind.ai.LLMGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("aiSchedulingStrategy")
@ConditionalOnProperty(name = "scheduling.strategy", havingValue = "ai")
public class AiSchedulingStrategy implements SchedulingStrategy {
    private final LLMGateway llmGateway;
    private final ObjectMapper objectMapper;

    public AiSchedulingStrategy(LLMGateway llmGateway, ObjectMapper objectMapper) {
        this.llmGateway = llmGateway;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<SchedulingRecommendation> recommend(
            Appointment appointment,
            List<Slot> openSlots,
            List<Provider> providers,
            TriggerReason reason) {

        List<Slot> candidates = eligibleSlots(appointment, openSlots, reason);
        if (candidates.isEmpty()) return List.of();

        String slotData = candidates.stream().map(this::slotAsJson)
                .collect(Collectors.joining("\n"));
        String providerData = providers.stream()
                .map(p -> p.getId() + ":" + p.getName() + ":" + p.getSpecialty() + ":" + p.getStatus())
                .collect(Collectors.joining("\n"));

        String triggerInstructions = switch (reason) {
            case PROVIDER_OFFLINE ->
                    "The original provider is OFFLINE. Select an OPEN slot with a different AVAILABLE provider of the same specialty. Do not recommend the offline provider.";
            case SLOT_CANCELLED ->
                    "The booked slot was cancelled. Prefer an OPEN replacement slot with the SAME provider. Use a matching-specialty alternate provider only if no same-provider slot is available.";
            case INITIAL ->
                    "For an initial suggestion, prefer the nearest eligible OPEN slot with the current provider. If none exists, use an AVAILABLE provider of the same specialty.";
        };

        String prompt = """
                Recommend one replacement appointment slot using only the candidates below.
                Trigger reason: %s
                Recovery policy: %s
                Appointment: id=%s, patient=%s, type/urgency=%s
                Current provider: id=%s, name=%s, specialty=%s, status=%s
                Current slot start: %s
                
                Eligible OPEN slot candidates (the only slot IDs allowed):
                %s
                
                Provider roster:
                %s
                
                Return exactly one JSON object, no Markdown:
                {"slotId":"existing-slot-id","alternateProviderId":null,"confidence":0.0,"reasoning":"Patient-friendly explanation."}
                Rules:
                - slotId must exactly match one candidate.
                - alternateProviderId is null if the selected slot belongs to the current provider; otherwise it must equal that slot's provider ID.
                - confidence must be a number between 0 and 1.
                - Do not invent IDs or times and do not claim the booking is finalized.
                - Explain the recommendation in simple, reassuring language for front-desk staff to share with the patient.
                """.formatted(reason, triggerInstructions, appointment.getId(), appointment.getPatientName(),
                appointment.getType(), appointment.getProvider().getId(), appointment.getProvider().getName(),
                appointment.getProvider().getSpecialty(), appointment.getProvider().getStatus(),
                appointment.getSlot().getStartTime(), slotData, providerData);

        String answer = llmGateway.callLLM(prompt);
        SchedulingRecommendation recommendation = parseAndValidate(answer, appointment, candidates, reason);
        return recommendation == null ? List.of() : List.of(recommendation);
    }

    private List<Slot> eligibleSlots(Appointment appointment, List<Slot> slots, TriggerReason reason) {
        List<Slot> eligible = new ArrayList<>();
        for (Slot slot : slots) {
            if (slot == null || slot.getId() == null || slot.getStatus() != SlotStatus.OPEN
                    || slot.getProvider() == null || slot.getProvider().getStatus() != ProviderStatus.AVAILABLE
                    || slot.getRoom() == null || !slot.getRoom().isAvailable()
                    || slot.getStartTime().isBefore(appointment.getSlot().getStartTime())) continue;

            boolean sameProvider = slot.getProvider().getId().equals(appointment.getProvider().getId());
            boolean sameSpecialty = slot.getProvider().getSpecialty() == appointment.getProvider().getSpecialty();
            if (!sameProvider && !sameSpecialty) continue;
            if (reason == TriggerReason.PROVIDER_OFFLINE && sameProvider) continue;
            eligible.add(slot);
        }
        if (reason == TriggerReason.SLOT_CANCELLED) {
            boolean sameProviderSlotExists = eligible.stream()
                    .anyMatch(s -> s.getProvider().getId().equals(appointment.getProvider().getId()));
            if (sameProviderSlotExists) {
                eligible.removeIf(s -> !s.getProvider().getId().equals(appointment.getProvider().getId()));
            }
        }
        return eligible;
    }

    private SchedulingRecommendation parseAndValidate(
            String answer, Appointment appointment, List<Slot> candidates, TriggerReason reason) {
        try {
            String cleaned = answer == null ? "" : answer.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
            }
            int first = cleaned.indexOf('{');
            int last = cleaned.lastIndexOf('}');
            if (first < 0 || last <= first)
                throw new IllegalArgumentException("AI response did not contain a JSON object");
            JsonNode node = objectMapper.readTree(cleaned.substring(first, last + 1));

            String slotId = textOrNull(node.path("slotId"));
            if (slotId == null) return null;
            Slot selected = candidates.stream().filter(s -> s.getId().equals(slotId)).findFirst().orElse(null);
            if (selected == null || selected.getStatus() != SlotStatus.OPEN
                    || selected.getProvider().getStatus() != ProviderStatus.AVAILABLE
                    || selected.getRoom() == null || !selected.getRoom().isAvailable()) return null;

            boolean sameProvider = selected.getProvider().getId().equals(appointment.getProvider().getId());
            if (!sameProvider && selected.getProvider().getSpecialty() != appointment.getProvider().getSpecialty())
                return null;
            if (reason == TriggerReason.PROVIDER_OFFLINE && sameProvider) return null;
            if (reason == TriggerReason.SLOT_CANCELLED) {
                boolean sameProviderExists = candidates.stream()
                        .anyMatch(s -> s.getProvider().getId().equals(appointment.getProvider().getId()));
                if (sameProviderExists && !sameProvider) return null;
            }

            String alternateId = textOrNull(node.path("alternateProviderId"));
            if (sameProvider) alternateId = null;
            else if (!selected.getProvider().getId().equals(alternateId)) return null;

            JsonNode confidenceNode = node.path("confidence");
            if (!confidenceNode.isNumber()) return null;
            double confidence = confidenceNode.asDouble();
            if (confidence < 0 || confidence > 1) return null;
            String reasoning = textOrNull(node.path("reasoning"));
            if (reasoning == null) reasoning = "A suitable replacement slot is available for your appointment.";
            if (reasoning.length() > 500) reasoning = reasoning.substring(0, 497) + "...";
            return new SchedulingRecommendation(slotId, alternateId, confidence, reasoning);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Could not parse or validate the AI scheduling response", exception);
        }
    }

    private String slotAsJson(Slot slot) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("slotId", slot.getId());
        data.put("startTime", slot.getStartTime().toString());
        data.put("durationMinutes", slot.getDurationMinutes());
        data.put("providerId", slot.getProvider().getId());
        data.put("providerName", slot.getProvider().getName());
        data.put("specialty", slot.getProvider().getSpecialty().name());
        data.put("providerStatus", slot.getProvider().getStatus().name());
        data.put("room", slot.getRoom().getName());
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize slot candidate", exception);
        }
    }

    private String textOrNull(JsonNode node) {
        return node != null && node.isTextual() && !node.asText().isBlank() ? node.asText().trim() : null;
    }
}
