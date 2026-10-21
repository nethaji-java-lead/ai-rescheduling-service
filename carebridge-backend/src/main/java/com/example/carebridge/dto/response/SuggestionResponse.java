package com.example.carebridge.dto.response;
import com.example.carebridge.enums.*;
import com.example.carebridge.model.RescheduleSuggestion;
import java.time.LocalDateTime;
public record SuggestionResponse(Long id, Long appointmentId, Long recommendedSlotId, LocalDateTime recommendedStartTime,
 Long alternateProviderId, String alternateProviderName, double confidence, String reasoning,
 SuggestionStatus status, TriggerReason triggerReason) {
 public static SuggestionResponse from(RescheduleSuggestion s) {
  var p = s.getAlternateProvider();
  return new SuggestionResponse(s.getId(), s.getAppointment().getId(), s.getRecommendedSlot().getId(),
   s.getRecommendedSlot().getStartTime(), p == null ? null : p.getId(), p == null ? null : p.getName(),
   s.getConfidence(), s.getReasoning(), s.getStatus(), s.getTriggerReason());
 }
}
