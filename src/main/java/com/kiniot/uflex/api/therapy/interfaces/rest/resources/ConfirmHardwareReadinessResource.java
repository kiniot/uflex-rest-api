package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

public record ConfirmHardwareReadinessResource(
        @NotNull Boolean sensorsPlaced
) {}
