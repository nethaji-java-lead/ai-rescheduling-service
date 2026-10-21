package com.carebridge.dto.request;

import com.carebridge.enums.AppointmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAppointmentRequest(@NotBlank String patientName, @NotNull AppointmentType type,
                                       @NotNull String providerId, @NotNull String slotId) {
}
