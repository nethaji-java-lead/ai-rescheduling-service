package com.carebridge.dto.response;
import com.carebridge.enums.SlotStatus;
import com.carebridge.model.Slot;
import java.time.LocalDateTime;
public record SlotResponse(String id, LocalDateTime startTime, int durationMinutes, String providerId,
 String providerName, Long roomId, String roomName, SlotStatus status) {
 public static SlotResponse from(Slot s) { return new SlotResponse(s.getId(), s.getStartTime(), s.getDurationMinutes(),
  s.getProvider().getId(), s.getProvider().getName(), s.getRoom().getId(), s.getRoom().getName(), s.getStatus()); }
}
