package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RecordCompensatoryMovementResource(
        @NotBlank @Pattern(regexp = "ShoulderCompensation|TrunkCompensation") String type
) {}
