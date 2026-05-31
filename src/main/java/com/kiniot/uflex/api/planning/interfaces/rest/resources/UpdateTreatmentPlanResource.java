package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateTreatmentPlanResource(
        @Schema(description = "Treatment plan name", example = "Forearm mobility plan - adjusted")
        String name,
        @Schema(description = "Real status of the treatment plan", allowableValues = {"SCHEDULED", "ACTIVE", "COMPLETED", "CANCELED"}, example = "ACTIVE")
        String status,
        TreatmentPlanPeriodResource period
) {
}
