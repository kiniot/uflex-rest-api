package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record TreatmentPlanPeriodResource(
        @Schema(description = "Planned start date for the treatment plan", example = "2026-06-01")
        String startsAt,
        @Schema(description = "Planned end date for the treatment plan", example = "2026-06-21")
        String endsAt
) {
}
