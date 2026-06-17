package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TreatmentPlanResource(
        @Schema(example = "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c")
        String id,
        @Schema(example = "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c")
        String patientId,
        @Schema(example = "Forearm mobility plan")
        String name,
        @Schema(allowableValues = {"SCHEDULED", "ACTIVE", "COMPLETED", "CANCELED"}, example = "ACTIVE")
        String status,
        TreatmentPlanPeriodResource period,
        List<RoutineResource> routines
) {
}
