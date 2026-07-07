package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record RecordValidRepetitionResource(
        @NotNull @DecimalMin("0.0") @DecimalMax("180.0") Double peakAngle,
        @NotNull @DecimalMin("0.0") @DecimalMax("180.0") Double achievedRom,
        @NotBlank @Pattern(regexp = "Good|Incomplete|Unsafe") String classification,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime recordedAt
) {}
