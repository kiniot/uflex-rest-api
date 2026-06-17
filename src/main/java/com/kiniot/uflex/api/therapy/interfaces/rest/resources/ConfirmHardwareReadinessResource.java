package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmHardwareReadinessResource(
        @NotBlank String deviceId,
        @NotNull Boolean sensorsPlaced
) {}
