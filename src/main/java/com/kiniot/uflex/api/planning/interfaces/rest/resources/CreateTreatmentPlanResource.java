package com.kiniot.uflex.api.planning.interfaces.rest.resources;

public record CreateTreatmentPlanResource(
        String id,
        String name,
        PlanFrequencyResource frequency
) {
}
