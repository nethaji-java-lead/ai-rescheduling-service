package com.example.carebridge.dto.response;
import com.example.carebridge.enums.*;
import com.example.carebridge.model.Appointment;
import java.time.LocalDateTime;
public record AppointmentResponse(Long id, String patientName, AppointmentType type, Long providerId,
 String providerName, Long slotId, LocalDateTime slotStartTime, AppointmentStatus status) {
 public static AppointmentResponse from(Appointment a) {
  return new AppointmentResponse(a.getId(), a.getPatientName(), a.getType(), a.getProvider().getId(),
   a.getProvider().getName(), a.getSlot().getId(), a.getSlot().getStartTime(), a.getStatus());
 }
}
