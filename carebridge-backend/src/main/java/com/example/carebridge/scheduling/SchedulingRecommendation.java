package com.example.carebridge.scheduling;
public record SchedulingRecommendation(Long slotId, Long alternateProviderId, double confidence, String reasoning) {}
