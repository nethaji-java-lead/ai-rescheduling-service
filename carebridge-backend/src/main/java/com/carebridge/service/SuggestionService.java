package com.carebridge.service;

import com.carebridge.agent.RescheduleEvent;
import com.carebridge.dto.response.SuggestionResponse;
import com.carebridge.enums.*;
import com.carebridge.exception.ConflictException;
import com.carebridge.exception.ResourceNotFoundException;
import com.carebridge.model.Appointment;
import com.carebridge.model.Provider;
import com.carebridge.model.Slot;
import com.carebridge.repository.AppointmentRepository;
import com.carebridge.repository.ProviderRepository;
import com.carebridge.repository.SlotRepository;
import com.carebridge.repository.SuggestionRepository;
import com.carebridge.scheduling.SchedulingEngine;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class SuggestionService {
 private final SuggestionRepository suggestions; private final AppointmentRepository appointments; private final ProviderRepository providers;
 private final SlotRepository slots; private final SchedulingEngine engine; private final ApplicationEventPublisher events;
 public SuggestionService(SuggestionRepository suggestions, AppointmentRepository appointments, ProviderRepository providers,
   SlotRepository slots, SchedulingEngine engine, ApplicationEventPublisher events) {
  this.suggestions=suggestions; this.appointments=appointments; this.providers=providers; this.slots=slots; this.engine=engine; this.events=events;
 }
 @Transactional
 public List<SuggestionResponse> suggest(String appointmentId) {
  var a=appointments.findByIdForUpdate(appointmentId).orElseThrow(()->new ResourceNotFoundException("Appointment not found"));
  events.publishEvent(new RescheduleEvent(TriggerReason.INITIAL, null, null, appointmentId));
  return suggestions.findByAppointmentIdAndStatus(appointmentId, SuggestionStatus.PENDING).stream().map(SuggestionResponse::from).toList();
 }
 @Transactional
 public SuggestionResponse decide(Long suggestionId, SuggestionStatus decision) {
  if (decision!=SuggestionStatus.ACCEPTED && decision!=SuggestionStatus.REJECTED) throw new IllegalArgumentException("Decision must be ACCEPTED or REJECTED");
  var s=suggestions.findById(suggestionId).orElseThrow(()->new ResourceNotFoundException("Suggestion not found"));
  if (s.getStatus()!=SuggestionStatus.PENDING) throw new ConflictException("Suggestion is no longer pending");
  if (decision==SuggestionStatus.REJECTED) { s.setStatus(SuggestionStatus.REJECTED); return SuggestionResponse.from(s); }
  Appointment a=appointments.findByIdForUpdate(s.getAppointment().getId()).orElseThrow(()->new ResourceNotFoundException("Appointment not found"));
  if (a.getStatus()!= AppointmentStatus.RESCHEDULE_PENDING) throw new ConflictException("Appointment is not pending rescheduling");
  Slot newSlot=slots.findByIdForUpdate(s.getRecommendedSlot().getId()).orElseThrow(()->new ResourceNotFoundException("Recommended slot not found"));
  if (newSlot.getStatus()!= SlotStatus.OPEN) throw new ConflictException("Recommended slot is no longer open");
  Provider newProvider=newSlot.getProvider();
  if (newProvider.getStatus()!= ProviderStatus.AVAILABLE || !newSlot.getRoom().isAvailable()) throw new ConflictException("Provider or room is unavailable");
  Slot oldSlot=slots.findByIdForUpdate(a.getSlot().getId()).orElseThrow(()->new ResourceNotFoundException("Previous slot not found"));
  newSlot.setStatus(SlotStatus.BOOKED);
  if (oldSlot.getId().equals(newSlot.getId())) throw new ConflictException("Replacement slot must differ from current slot");
  if (oldSlot.getStatus()==SlotStatus.BOOKED) oldSlot.setStatus(SlotStatus.OPEN);
  a.completeReschedule(newProvider, newSlot);
  s.setStatus(SuggestionStatus.ACCEPTED);
  suggestions.findByAppointmentIdAndStatus(a.getId(), SuggestionStatus.PENDING).stream()
    .filter(other -> !other.getId().equals(s.getId())).forEach(other -> other.setStatus(SuggestionStatus.REJECTED));
  return SuggestionResponse.from(s);
 }
}
