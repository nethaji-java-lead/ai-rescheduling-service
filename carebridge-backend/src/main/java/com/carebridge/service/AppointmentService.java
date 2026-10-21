package com.carebridge.service;

import com.carebridge.dto.request.CreateAppointmentRequest;
import com.carebridge.dto.response.AppointmentResponse;
import com.carebridge.enums.AppointmentStatus;
import com.carebridge.enums.ProviderStatus;
import com.carebridge.enums.SlotStatus;
import com.carebridge.exception.ConflictException;
import com.carebridge.exception.ResourceNotFoundException;
import com.carebridge.model.Appointment;
import com.carebridge.model.Provider;
import com.carebridge.model.Slot;
import com.carebridge.repository.AppointmentRepository;
import com.carebridge.repository.ProviderRepository;
import com.carebridge.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class AppointmentService {
 private final AppointmentRepository appointments; private final ProviderRepository providers; private final SlotRepository slots;
 public AppointmentService(AppointmentRepository appointments, ProviderRepository providers, SlotRepository slots) {
  this.appointments=appointments; this.providers=providers; this.slots=slots;
 }
 @Transactional
 public AppointmentResponse create(CreateAppointmentRequest request) {
  Provider provider = providers.findById(request.providerId()).orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
  if (provider.getStatus() != ProviderStatus.AVAILABLE) throw new ConflictException("Provider is not available");
  Slot slot = slots.findByIdForUpdate(request.slotId()).orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
  if (slot.getStatus() != SlotStatus.OPEN) throw new ConflictException("Slot is not open");
  if (!slot.getProvider().getId().equals(provider.getId())) throw new ConflictException("Slot does not belong to the selected provider");
  if (!slot.getRoom().isAvailable()) throw new ConflictException("Room is unavailable");
  slot.setStatus(SlotStatus.BOOKED);
  return AppointmentResponse.from(appointments.save(new Appointment(request.patientName().trim(), request.type(), provider, slot)));
 }
 @Transactional(readOnly=true)
 public List<AppointmentResponse> list(AppointmentStatus status) {
  List<Appointment> result = status == null ? appointments.findAll() : appointments.findByStatusOrderByCreatedAtDesc(status);
  return result.stream().map(AppointmentResponse::from).toList();
 }
}
