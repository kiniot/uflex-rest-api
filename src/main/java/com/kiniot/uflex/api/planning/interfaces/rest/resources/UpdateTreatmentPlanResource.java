package com.kiniot.uflex.api.planning.interfaces.rest.resources;

public record UpdateTreatmentPlanResource(
        String name,
        PlanFrequencyResource frequency
) {
}
