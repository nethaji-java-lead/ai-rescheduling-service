package com.carebridge.agent;

import com.carebridge.enums.TriggerReason;

public record RescheduleEvent(TriggerReason triggerReason, String providerId, String slotId, String appointmentId) {
}
