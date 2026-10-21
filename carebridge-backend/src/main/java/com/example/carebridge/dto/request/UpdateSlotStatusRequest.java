package com.example.carebridge.dto.request;
import com.example.carebridge.enums.SlotStatus;
import jakarta.validation.constraints.NotNull;
public record UpdateSlotStatusRequest(@NotNull SlotStatus status) {}
