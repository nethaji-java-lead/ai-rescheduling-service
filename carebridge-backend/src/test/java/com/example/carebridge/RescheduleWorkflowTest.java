package com.example.carebridge;

import com.example.carebridge.enums.AppointmentStatus;
import com.example.carebridge.enums.SlotStatus;
import com.example.carebridge.enums.SuggestionStatus;
import com.example.carebridge.enums.TriggerReason;
import com.example.carebridge.model.RescheduleSuggestion;
import com.example.carebridge.repository.AppointmentRepository;
import com.example.carebridge.repository.SlotRepository;
import com.example.carebridge.repository.SuggestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RescheduleWorkflowTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private AppointmentRepository appointments;
    @Autowired private SlotRepository slots;
    @Autowired private SuggestionRepository suggestions;

    @Test
    void providerAndRoomOutagesCreateSuggestionsAndAcceptanceMovesAppointmentAtomically() throws Exception {
        mockMvc.perform(patch("/providers/1/status")
                .contentType("application/json")
                .content("{\"status\":\"OFFLINE\"}"))
            .andExpect(status().isOk());

        RescheduleSuggestion first = awaitSuggestion(1L);
        RescheduleSuggestion second = awaitSuggestion(2L);
        assertEquals(TriggerReason.PROVIDER_OFFLINE, first.getTriggerReason());
        assertEquals(4L, first.getRecommendedSlot().getProvider().getId());

        mockMvc.perform(patch("/suggestions/{id}", first.getId())
                .contentType("application/json")
                .content("{\"decision\":\"ACCEPTED\"}"))
            .andExpect(status().isOk());

        var rescheduled = appointments.findById(1L).orElseThrow();
        assertEquals(AppointmentStatus.RESCHEDULED, rescheduled.getStatus());
        assertEquals(4L, rescheduled.getProvider().getId());
        assertEquals(SlotStatus.BOOKED, slots.findById(5L).orElseThrow().getStatus());
        assertEquals(SlotStatus.CANCELLED, slots.findById(1L).orElseThrow().getStatus());

        mockMvc.perform(patch("/suggestions/{id}", second.getId())
                .contentType("application/json")
                .content("{\"decision\":\"ACCEPTED\"}"))
            .andExpect(status().isConflict());

        mockMvc.perform(patch("/rooms/3/availability")
                .contentType("application/json")
                .content("{\"available\":false}"))
            .andExpect(status().isOk());
        RescheduleSuggestion roomSuggestion = awaitSuggestion(4L);
        assertEquals(TriggerReason.ROOM_UNAVAILABLE, roomSuggestion.getTriggerReason());

        mockMvc.perform(patch("/suggestions/{id}", roomSuggestion.getId())
                .contentType("application/json")
                .content("{\"decision\":\"ACCEPTED\"}"))
            .andExpect(status().isOk());
        assertEquals(AppointmentStatus.RESCHEDULED, appointments.findById(4L).orElseThrow().getStatus());
        assertEquals(SlotStatus.BOOKED, slots.findById(4L).orElseThrow().getStatus());
        assertEquals(SlotStatus.CANCELLED, slots.findById(6L).orElseThrow().getStatus());

        mockMvc.perform(patch("/slots/6/status")
                .contentType("application/json")
                .content("{\"status\":\"OPEN\"}"))
            .andExpect(status().isConflict());

        mockMvc.perform(patch("/slots/3/status")
                .contentType("application/json")
                .content("{\"status\":\"CANCELLED\"}"))
            .andExpect(status().isOk());
        RescheduleSuggestion cancelledSlotSuggestion = awaitSuggestion(3L);
        assertEquals(TriggerReason.SLOT_CANCELLED, cancelledSlotSuggestion.getTriggerReason());
        mockMvc.perform(patch("/suggestions/{id}", cancelledSlotSuggestion.getId())
                .contentType("application/json")
                .content("{\"decision\":\"ACCEPTED\"}"))
            .andExpect(status().isOk());
        assertEquals(AppointmentStatus.RESCHEDULED, appointments.findById(3L).orElseThrow().getStatus());
        assertEquals(SlotStatus.BOOKED, slots.findById(7L).orElseThrow().getStatus());
    }

    private RescheduleSuggestion awaitSuggestion(Long appointmentId) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            var pending = suggestions.findByAppointmentIdAndStatus(appointmentId, SuggestionStatus.PENDING);
            if (!pending.isEmpty()) return pending.get(0);
            Thread.sleep(50);
        }
        fail("No pending suggestion was generated for appointment " + appointmentId);
        return null;
    }
}
