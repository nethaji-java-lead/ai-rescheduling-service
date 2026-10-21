package com.carebridge.dto.response;

import com.carebridge.enums.SuggestionStatus;
import com.carebridge.enums.TriggerReason;
import com.carebridge.model.RescheduleSuggestion;

import java.time.LocalDateTime;

public record SuggestionResponse(Long id, String appointmentId, String recommendedSlotId,
                                 LocalDateTime recommendedStartTime,
                                 String alternateProviderId, String alternateProviderName, double confidence,
                                 String reasoning,
                                 SuggestionStatus status, TriggerReason triggerReason) {
    public static SuggestionResponse from(RescheduleSuggestion s) {
        var p = s.getAlternateProvider();
        return new SuggestionResponse(s.getId(), s.getAppointment().getId(), s.getRecommendedSlot().getId(),
                s.getRecommendedSlot().getStartTime(), p == null ? null : p.getId(), p == null ? null : p.getName(),
                s.getConfidence(), s.getReasoning(), s.getStatus(), s.getTriggerReason());
    }
}
