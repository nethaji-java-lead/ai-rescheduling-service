package com.example.carebridge.agent;
import com.example.carebridge.enums.TriggerReason;
public record RescheduleEvent(TriggerReason triggerReason, Long providerId, Long slotId, Long appointmentId) {}
