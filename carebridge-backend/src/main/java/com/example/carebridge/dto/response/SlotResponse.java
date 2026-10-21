package com.example.carebridge.dto.response;
import com.example.carebridge.enums.SlotStatus;
import com.example.carebridge.model.Slot;
import java.time.LocalDateTime;
public record SlotResponse(Long id, LocalDateTime startTime, int durationMinutes, Long providerId,
 String providerName, Long roomId, String roomName, SlotStatus status) {
 public static SlotResponse from(Slot s) { return new SlotResponse(s.getId(), s.getStartTime(), s.getDurationMinutes(),
  s.getProvider().getId(), s.getProvider().getName(), s.getRoom().getId(), s.getRoom().getName(), s.getStatus()); }
}
