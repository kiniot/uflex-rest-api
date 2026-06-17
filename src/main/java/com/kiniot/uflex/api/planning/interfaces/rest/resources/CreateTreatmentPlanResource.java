package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CreateTreatmentPlanResource(
        @Schema(description = "Treatment plan name", example = "Forearm mobility plan")
        String name,
        TreatmentPlanPeriodResource period,
        List<CreateTreatmentPlanRoutineResource> routines
) {
}
