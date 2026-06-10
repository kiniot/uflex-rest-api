package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RecordValidRepetitionResource(
        @NotNull @DecimalMin("0.0") @DecimalMax("180.0") Double achievedAngle,
        @NotNull Instant recordedAt
) {}
