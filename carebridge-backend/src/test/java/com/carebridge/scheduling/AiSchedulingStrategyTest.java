package com.carebridge.scheduling;

import com.carebridge.enums.*;
import com.carebridge.model.Appointment;
import com.carebridge.model.Provider;
import com.carebridge.model.Room;
import com.carebridge.model.Slot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slotmind.ai.LLMGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiSchedulingStrategyTest {
    private StubLlmGateway llmGateway;
    private AiSchedulingStrategy strategy;
    private Appointment appointment;
    private Provider currentProvider;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        ObjectMapper objectMapper = new ObjectMapper();
        llmGateway = new StubLlmGateway(objectMapper);
        strategy = new AiSchedulingStrategy(llmGateway, objectMapper);

        currentProvider = provider("PRV-001", "Dr. Sarah Chen", ProviderStatus.AVAILABLE);
        Slot currentSlot = slot("SLT-001", LocalDateTime.of(2026, 11, 1, 9, 0),
                currentProvider, true);
        appointment = new Appointment("Taylor Smith", AppointmentType.ROUTINE, currentProvider, currentSlot);
        setId(appointment, "APT-001");
    }

    @Test
    void recommendsValidCandidateAndBuildsPromptWithSchedulingContext() throws Exception {
        Slot candidate = slot("SLT-002", LocalDateTime.of(2026, 11, 1, 10, 0),
                currentProvider, true);
        llmGateway.response = """
                {"slotId":"SLT-002","alternateProviderId":null,"confidence":0.93,
                 "reasoning":"Your doctor has an opening at 10 AM."}
                """;

        List<SchedulingRecommendation> recommendations = strategy.recommend(
                appointment, List.of(candidate), List.of(currentProvider), TriggerReason.INITIAL);

        assertEquals(1, recommendations.size());
        SchedulingRecommendation result = recommendations.get(0);
        assertEquals("SLT-002", result.slotId());
        assertNull(result.alternateProviderId());
        assertEquals(0.93, result.confidence());
        assertEquals("Your doctor has an opening at 10 AM.", result.reasoning());
        assertEquals(1, llmGateway.calls);
        assertTrue(llmGateway.prompt.contains("APT-001"));
        assertTrue(llmGateway.prompt.contains("SLT-002"));
    }

    @Test
    void doesNotCallGatewayWhenThereAreNoEligibleCandidates() throws Exception {
        Slot unavailable = slot("SLT-002", LocalDateTime.of(2026, 11, 1, 10, 0),
                currentProvider, false);

        List<SchedulingRecommendation> recommendations = strategy.recommend(
                appointment, List.of(unavailable), List.of(currentProvider), TriggerReason.INITIAL);

        assertTrue(recommendations.isEmpty());
        assertEquals(0, llmGateway.calls);
    }

    @Test
    void rejectsModelResponseThatInventsSlotId() throws Exception {
        Slot candidate = slot("SLT-002", LocalDateTime.of(2026, 11, 1, 10, 0),
                currentProvider, true);
        llmGateway.response = """
                {"slotId":"SLT-999","alternateProviderId":null,"confidence":0.9,"reasoning":"Invented slot"}
                """;

        List<SchedulingRecommendation> recommendations = strategy.recommend(
                appointment, List.of(candidate), List.of(currentProvider), TriggerReason.INITIAL);

        assertTrue(recommendations.isEmpty());
    }

    @Test
    void providerOfflineRequiresAvailableAlternateOfSameSpecialty() throws Exception {
        currentProvider.setStatus(ProviderStatus.OFFLINE);
        Provider alternate = provider("PRV-004", "Dr. Rahul Verma", ProviderStatus.AVAILABLE);
        Slot sameProviderCandidate = slot("SLT-002", LocalDateTime.of(2026, 11, 1, 10, 0),
                currentProvider, true);
        Slot alternateCandidate = slot("SLT-005", LocalDateTime.of(2026, 11, 1, 11, 0),
                alternate, true);
        llmGateway.response = """
                {"slotId":"SLT-005","alternateProviderId":"PRV-004","confidence":0.81,
                 "reasoning":"A colleague in the same specialty can see you at 11 AM."}
                """;

        List<SchedulingRecommendation> recommendations = strategy.recommend(
                appointment, List.of(sameProviderCandidate, alternateCandidate),
                List.of(currentProvider, alternate), TriggerReason.PROVIDER_OFFLINE);

        assertEquals(1, recommendations.size());
        assertEquals("SLT-005", recommendations.get(0).slotId());
        assertEquals("PRV-004", recommendations.get(0).alternateProviderId());
        assertTrue(llmGateway.prompt.contains("SLT-005"));
        assertFalse(llmGateway.prompt.contains("SLT-002"));
    }

    @Test
    void rejectsMalformedModelResponse() throws Exception {
        Slot candidate = slot("SLT-002", LocalDateTime.of(2026, 11, 1, 10, 0),
                currentProvider, true);
        llmGateway.response = "not JSON";

        assertThrows(IllegalArgumentException.class, () -> strategy.recommend(
                appointment, List.of(candidate), List.of(currentProvider), TriggerReason.INITIAL));
    }

    private Provider provider(String id, String name, ProviderStatus status)
            throws ReflectiveOperationException {
        Provider provider = new Provider(name, ProviderSpecialty.GENERAL, status);
        setId(provider, id);
        return provider;
    }

    private Slot slot(String id, LocalDateTime startTime, Provider provider, boolean roomAvailable)
            throws ReflectiveOperationException {
        Room room = new Room("Room 101", roomAvailable);
        setId(room, 1L);
        Slot slot = new Slot(startTime, 30, provider, room, SlotStatus.OPEN);
        setId(slot, id);
        return slot;
    }

    private void setId(Object entity, Object id) throws ReflectiveOperationException {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    private static final class StubLlmGateway extends LLMGateway {
        private String response;
        private String prompt;
        private int calls;

        private StubLlmGateway(ObjectMapper objectMapper) {
            super(RestClient.builder(), objectMapper, "bionic", "http://localhost:1234/v1", "", "test-model");
        }

        @Override
        public String callLLM(String prompt) {
            this.prompt = prompt;
            calls++;
            return response;
        }
    }
}
