package com.carebridge.dto.response;
import com.carebridge.enums.AppointmentStatus;
import com.carebridge.enums.AppointmentType;
import com.carebridge.model.Appointment;
import java.time.LocalDateTime;
public record AppointmentResponse(String id, String patientName, AppointmentType type, String providerId,
                                  String providerName, String slotId, LocalDateTime slotStartTime, AppointmentStatus status) {
 public static AppointmentResponse from(Appointment a) {
  return new AppointmentResponse(a.getId(), a.getPatientName(), a.getType(), a.getProvider().getId(),
   a.getProvider().getName(), a.getSlot().getId(), a.getSlot().getStartTime(), a.getStatus());
 }
}
