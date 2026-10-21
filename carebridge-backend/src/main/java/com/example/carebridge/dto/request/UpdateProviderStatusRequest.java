package com.example.carebridge.dto.request;
import com.example.carebridge.enums.ProviderStatus;
import jakarta.validation.constraints.NotNull;
public record UpdateProviderStatusRequest(@NotNull ProviderStatus status) {}
