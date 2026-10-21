package com.example.carebridge.agent;

import com.example.carebridge.enums.*;
import com.example.carebridge.model.*;
import com.example.carebridge.repository.*;
import com.example.carebridge.scheduling.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Component
public class RescheduleWorker {
 private static final Logger log = LoggerFactory.getLogger(RescheduleWorker.class);
 private final AppointmentRepository appointments; private final ProviderRepository providers; private final SlotRepository slots;
 private final SuggestionRepository suggestions; private final SchedulingEngine engine;
 public RescheduleWorker(AppointmentRepository appointments, ProviderRepository providers, SlotRepository slots,
   SuggestionRepository suggestions, SchedulingEngine engine) {
  this.appointments=appointments; this.providers=providers; this.slots=slots; this.suggestions=suggestions; this.engine=engine;
 }
 @Transactional
 public void process(RescheduleEvent event) {
  List<Appointment> affected;
  if (event.appointmentId()!=null) {
   affected=appointments.findById(event.appointmentId()).filter(a -> a.getStatus()==AppointmentStatus.CONFIRMED || a.getStatus()==AppointmentStatus.RESCHEDULE_PENDING).map(List::of).orElse(List.of());
  } else if (event.providerId()!=null) {
   affected=appointments.findByProviderIdAndStatusInOrderByIdAsc(event.providerId(),
     List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULE_PENDING));
  } else return;
  for (Appointment candidate : affected) {
   try {
    Appointment appointment=appointments.findByIdForUpdate(candidate.getId()).orElse(null);
    if (appointment==null || (appointment.getStatus()!=AppointmentStatus.CONFIRMED
      && appointment.getStatus()!=AppointmentStatus.RESCHEDULE_PENDING)) continue;
    if (event.providerId()!=null && !appointment.getProvider().getId().equals(event.providerId())) continue;
    if (suggestions.existsByAppointmentIdAndTriggerReasonAndStatus(appointment.getId(), event.triggerReason(), SuggestionStatus.PENDING)) continue;
    List<Slot> open=slots.findByStatusOrderByStartTimeAsc(SlotStatus.OPEN);
    List<Provider> roster=providers.findAll();
    List<SchedulingRecommendation> recommendations=engine.recommend(appointment, open, roster, event.triggerReason());
    SchedulingRecommendation chosen=recommendations==null ? null : recommendations.stream().filter(r -> r!=null && open.stream().anyMatch(s -> s.getId().equals(r.slotId())
       && s.getStatus()==SlotStatus.OPEN && s.getRoom().isAvailable()
       && s.getProvider().getStatus()==ProviderStatus.AVAILABLE
       && (s.getProvider().getId().equals(appointment.getProvider().getId())
         || s.getProvider().getSpecialty()==appointment.getProvider().getSpecialty())
       && (r.alternateProviderId()==null || r.alternateProviderId().equals(s.getProvider().getId()))))
      .findFirst().orElse(null);
    if (chosen==null) { log.warn("No valid replacement found for appointment {}", appointment.getId()); continue; }
    Slot recommended=slots.findById(chosen.slotId()).orElse(null);
    if (recommended==null) continue;
    Provider alternate = recommended.getProvider().getId().equals(appointment.getProvider().getId()) ? null : recommended.getProvider();
    if (appointment.getStatus()==AppointmentStatus.CONFIRMED) appointment.markReschedulePending();
    suggestions.save(new RescheduleSuggestion(appointment, recommended, alternate, chosen.confidence(), chosen.reasoning(), event.triggerReason()));
   } catch (Exception ex) {
    log.error("Rescheduling failed for appointment {} with trigger {}", candidate.getId(), event.triggerReason(), ex);
   }
  }
 }
}
