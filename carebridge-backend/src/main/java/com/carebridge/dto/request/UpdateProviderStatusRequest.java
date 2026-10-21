package com.carebridge.dto.request;

import com.carebridge.enums.ProviderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProviderStatusRequest(@NotNull ProviderStatus status) {
}
