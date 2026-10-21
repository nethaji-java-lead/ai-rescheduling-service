package com.example.carebridge.dto.request;
import com.example.carebridge.enums.AppointmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record CreateAppointmentRequest(@NotBlank String patientName, @NotNull AppointmentType type,
                                      @NotNull Long providerId, @NotNull Long slotId) {}
