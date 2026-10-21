package com.example.carebridge.service;
import com.example.carebridge.agent.RescheduleEvent;
import com.example.carebridge.dto.request.UpdateSlotStatusRequest;
import com.example.carebridge.dto.response.SlotResponse;
import com.example.carebridge.enums.*;
import com.example.carebridge.exception.*;
import com.example.carebridge.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class SlotService {
 private final SlotRepository slots; private final AppointmentRepository appointments; private final ApplicationEventPublisher events;
 public SlotService(SlotRepository slots, AppointmentRepository appointments, ApplicationEventPublisher events) { this.slots=slots; this.appointments=appointments; this.events=events; }
 @Transactional
 public SlotResponse updateStatus(Long id, UpdateSlotStatusRequest request) {
  var slot=slots.findByIdForUpdate(id).orElseThrow(()->new ResourceNotFoundException("Slot not found"));
  if (request.status()==SlotStatus.BOOKED) throw new ConflictException("A slot can only become BOOKED through appointment booking or acceptance");
  Long affectedAppointmentId = null;
  if (request.status()==SlotStatus.CANCELLED && slot.getStatus()==SlotStatus.BOOKED) {
   affectedAppointmentId=appointments.findBySlotIdAndStatusIn(id, java.util.List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULE_PENDING))
    .map(a->a.getId()).orElse(null);
  }
  slot.setStatus(request.status());
  if (request.status()==SlotStatus.CANCELLED && affectedAppointmentId!=null)
   events.publishEvent(new RescheduleEvent(TriggerReason.SLOT_CANCELLED, null, id, affectedAppointmentId));
  return SlotResponse.from(slot);
 }
}
