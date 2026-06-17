package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RecordAnomalousMovementResource(
        @NotBlank @Pattern(regexp = "ExcessiveMovement|AnomalousMovement") String alertType
) {}
