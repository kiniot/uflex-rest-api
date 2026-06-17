package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReportPainLevelResource(
        @NotNull @Min(0) @Max(10) Integer painLevel
) {}
