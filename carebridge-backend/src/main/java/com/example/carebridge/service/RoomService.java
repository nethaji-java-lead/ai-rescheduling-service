package com.example.carebridge.service;

import com.example.carebridge.agent.RescheduleEvent;
import com.example.carebridge.dto.response.RoomResponse;
import com.example.carebridge.enums.*;
import com.example.carebridge.exception.ResourceNotFoundException;
import com.example.carebridge.model.Room;
import com.example.carebridge.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {
    private final RoomRepository rooms;
    private final SlotRepository slots;
    private final AppointmentRepository appointments;
    private final ApplicationEventPublisher events;

    public RoomService(RoomRepository rooms, SlotRepository slots, AppointmentRepository appointments,
                       ApplicationEventPublisher events) {
        this.rooms = rooms;
        this.slots = slots;
        this.appointments = appointments;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> list() {
        return rooms.findAll().stream().map(RoomResponse::from).toList();
    }

    @Transactional
    public RoomResponse updateAvailability(Long id, boolean available) {
        Room room = rooms.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        boolean becameUnavailable = room.isAvailable() && !available;
        room.setAvailable(available);

        if (becameUnavailable) {
            for (var slot : slots.findByRoomIdAndStatus(id, SlotStatus.BOOKED)) {
                appointments.findBySlotIdAndStatusIn(slot.getId(),
                        List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULE_PENDING))
                    .ifPresent(appointment -> events.publishEvent(new RescheduleEvent(
                        TriggerReason.ROOM_UNAVAILABLE, null, slot.getId(), appointment.getId())));
            }
        }
        return RoomResponse.from(room);
    }
}
