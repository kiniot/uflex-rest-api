package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import java.util.List;

public record TreatmentPlanResource(
        String id,
        String name,
        PlanFrequencyResource frequency,
        String clinicId,
        List<RoutineResource> routines
) {
}
