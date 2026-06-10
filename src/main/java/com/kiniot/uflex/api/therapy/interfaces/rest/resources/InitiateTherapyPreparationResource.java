package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InitiateTherapyPreparationResource(
        @NotNull UUID patientId,
        @NotNull UUID treatmentPlanId,
        @NotBlank String iotDeviceId,
        @NotNull UUID routineId
) {}
