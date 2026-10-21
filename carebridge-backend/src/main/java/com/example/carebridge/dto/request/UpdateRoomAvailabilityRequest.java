package com.example.carebridge.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateRoomAvailabilityRequest(@NotNull Boolean available) {}
