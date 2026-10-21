package com.example.carebridge.scheduling;

import com.example.carebridge.enums.*;
import com.example.carebridge.model.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component("aiSchedulingStrategy")
@ConditionalOnProperty(name = "scheduling.strategy", havingValue = "ai")
public class AiSchedulingStrategy implements SchedulingStrategy {
 private final ChatClient chatClient;
 public AiSchedulingStrategy(ChatClient.Builder builder) { this.chatClient = builder.build(); }

 @Override
 public List<SchedulingRecommendation> recommend(Appointment appointment, List<Slot> openSlots, List<Provider> providers, TriggerReason reason) {
  String slotData = openSlots.stream().filter(s -> s.getStatus() == SlotStatus.OPEN && s.getRoom().isAvailable()
      && s.getProvider().getStatus() == ProviderStatus.AVAILABLE
      && (s.getProvider().getId().equals(appointment.getProvider().getId())
        || s.getProvider().getSpecialty() == appointment.getProvider().getSpecialty()))
   .map(s -> "slotId="+s.getId()+", start="+s.getStartTime()+", providerId="+s.getProvider().getId()+
     ", providerName="+s.getProvider().getName()+", specialty="+s.getProvider().getSpecialty()+
     ", providerStatus="+s.getProvider().getStatus()+", room="+s.getRoom().getName())
   .collect(Collectors.joining("\n"));
  String providerData = providers.stream().map(p -> p.getId()+":"+p.getName()+":"+p.getSpecialty()+":"+p.getStatus()).collect(Collectors.joining(", "));
  String triggerInstructions = reason == TriggerReason.SLOT_CANCELLED
   ? "Prefer a replacement slot with the same provider. Use another provider only if no suitable same-provider slot exists."
   : reason == TriggerReason.ROOM_UNAVAILABLE
    ? "The original room is unavailable; recommend an open slot in an available room with the same provider if possible."
    : "The original provider is unavailable; you may recommend an available alternate provider with matching specialty.";
  String answer = chatClient.prompt().system("""
    You are CareBridge's appointment scheduling advisor. Recommend only IDs present in the supplied data.
    Return exactly one JSON object with keys slotId (number), alternateProviderId (number or null),
    confidence (number from 0 to 1), reasoning (short patient-friendly explanation).
    Never invent IDs. Do not claim that a booking has been made.
    """).user("""
    Trigger: %s
    Policy: %s
    Appointment: id=%d, type=%s, currentProvider=%s, currentSpecialty=%s
    Available slots:
    %s
    Provider roster: %s
    """.formatted(reason, triggerInstructions, appointment.getId(), appointment.getType(),
      appointment.getProvider().getName(), appointment.getProvider().getSpecialty(), slotData, providerData))
    .call().content();
  try {
   String json = answer.replaceAll("(?s)^.*?\\{", "{").replaceAll("\\}.*$", "}");
   com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
   Long slotId = node.path("slotId").isNumber() ? node.path("slotId").asLong() : null;
   Long alternateId = node.path("alternateProviderId").isNumber() ? node.path("alternateProviderId").asLong() : null;
   double confidence = Math.max(0, Math.min(1, node.path("confidence").asDouble(0.5)));
   String reasoning = node.path("reasoning").asText("A suitable replacement slot is recommended.");
   if (slotId == null) return List.of();
   return List.of(new SchedulingRecommendation(slotId, alternateId, confidence, reasoning));
  } catch (Exception e) {
   throw new IllegalArgumentException("Could not parse scheduling AI response", e);
  }
 }
}
