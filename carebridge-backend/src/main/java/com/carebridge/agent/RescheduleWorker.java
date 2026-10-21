package com.carebridge.agent;

import com.carebridge.enums.AppointmentStatus;
import com.carebridge.enums.ProviderStatus;
import com.carebridge.enums.SlotStatus;
import com.carebridge.enums.SuggestionStatus;
import com.carebridge.model.Appointment;
import com.carebridge.model.Provider;
import com.carebridge.model.RescheduleSuggestion;
import com.carebridge.model.Slot;
import com.carebridge.repository.AppointmentRepository;
import com.carebridge.repository.ProviderRepository;
import com.carebridge.repository.SlotRepository;
import com.carebridge.repository.SuggestionRepository;
import com.carebridge.scheduling.SchedulingEngine;
import com.carebridge.scheduling.SchedulingRecommendation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class RescheduleWorker {
    private static final Logger log = LoggerFactory.getLogger(RescheduleWorker.class);
    private final AppointmentRepository appointments;
    private final ProviderRepository providers;
    private final SlotRepository slots;
    private final SuggestionRepository suggestions;
    private final SchedulingEngine engine;

    public RescheduleWorker(AppointmentRepository appointments, ProviderRepository providers, SlotRepository slots,
                            SuggestionRepository suggestions, SchedulingEngine engine) {
        this.appointments = appointments;
        this.providers = providers;
        this.slots = slots;
        this.suggestions = suggestions;
        this.engine = engine;
    }

    @Transactional
    public void process(RescheduleEvent event) {
        List<Appointment> affected;
        if (event.appointmentId() != null) {
            affected = appointments.findById(event.appointmentId()).filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED || a.getStatus() == AppointmentStatus.RESCHEDULE_PENDING).map(List::of).orElse(List.of());
        } else if (event.providerId() != null) {
            affected = appointments.findByProviderIdAndStatus(event.providerId(), AppointmentStatus.CONFIRMED);
        } else return;
        for (Appointment appointment : affected) {
            try {
                if (suggestions.existsByAppointmentIdAndTriggerReasonAndStatus(appointment.getId(), event.triggerReason(), SuggestionStatus.PENDING))
                    continue;
                List<Slot> open = slots.findByStatusOrderByStartTimeAsc(SlotStatus.OPEN);
                List<Provider> roster = providers.findAll();
                List<SchedulingRecommendation> recommendations = engine.recommend(appointment, open, roster, event.triggerReason());
                SchedulingRecommendation chosen = recommendations.stream().filter(r -> open.stream().anyMatch(s -> s.getId().equals(r.slotId())
                                && s.getStatus() == SlotStatus.OPEN && s.getRoom().isAvailable()
                                && s.getProvider().getStatus() == ProviderStatus.AVAILABLE
                                && (r.alternateProviderId() == null || r.alternateProviderId().equals(s.getProvider().getId()))))
                        .findFirst().orElse(null);
                if (chosen == null) {
                    log.warn("No valid replacement found for appointment {}", appointment.getId());
                    continue;
                }
                Slot recommended = slots.findById(chosen.slotId()).orElse(null);
                if (recommended == null) continue;
                Provider alternate = recommended.getProvider().getId().equals(appointment.getProvider().getId()) ? null : recommended.getProvider();
                if (appointment.getStatus() == AppointmentStatus.CONFIRMED) appointment.markReschedulePending();
                suggestions.save(new RescheduleSuggestion(appointment, recommended, alternate, chosen.confidence(), chosen.reasoning(), event.triggerReason()));
            } catch (Exception ex) {
                log.error("Rescheduling failed for appointment {} with trigger {}", appointment.getId(), event.triggerReason(), ex);
            }
        }
    }
}
