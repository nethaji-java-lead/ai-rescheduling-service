package com.carebridge.scheduling;

public record SchedulingRecommendation(String slotId, String alternateProviderId, double confidence, String reasoning) {
}
