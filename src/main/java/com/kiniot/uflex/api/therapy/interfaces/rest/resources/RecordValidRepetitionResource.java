package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RecordValidRepetitionResource(
        @NotNull @DecimalMin("0.0") @DecimalMax("180.0") Double achievedAngle,
        @NotNull  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime recordedAt
) {}
