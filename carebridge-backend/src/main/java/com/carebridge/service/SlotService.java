package com.carebridge.service;
import com.carebridge.agent.RescheduleEvent;
import com.carebridge.dto.request.UpdateSlotStatusRequest;
import com.carebridge.dto.response.SlotResponse;
import com.carebridge.enums.AppointmentStatus;
import com.carebridge.enums.SlotStatus;
import com.carebridge.enums.TriggerReason;
import com.carebridge.exception.ConflictException;
import com.carebridge.exception.ResourceNotFoundException;
import com.carebridge.repository.AppointmentRepository;
import com.carebridge.repository.SlotRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class SlotService {
 private final SlotRepository slots; private final AppointmentRepository appointments; private final ApplicationEventPublisher events;
 public SlotService(SlotRepository slots, AppointmentRepository appointments, ApplicationEventPublisher events) { this.slots=slots; this.appointments=appointments; this.events=events; }
 @Transactional
 public SlotResponse updateStatus(String id, UpdateSlotStatusRequest request) {
  var slot=slots.findByIdForUpdate(id).orElseThrow(()->new ResourceNotFoundException("Slot not found"));
  if (request.status()== SlotStatus.BOOKED) throw new ConflictException("A slot can only become BOOKED through appointment booking or acceptance");
  String affectedAppointmentId = null;
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
