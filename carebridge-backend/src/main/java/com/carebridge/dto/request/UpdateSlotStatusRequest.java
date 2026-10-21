package com.carebridge.dto.request;
import com.carebridge.enums.SlotStatus;
import jakarta.validation.constraints.NotNull;
public record UpdateSlotStatusRequest(@NotNull SlotStatus status) {}
