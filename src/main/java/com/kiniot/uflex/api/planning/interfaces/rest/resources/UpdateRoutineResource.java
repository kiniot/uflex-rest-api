package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import java.util.List;

public record UpdateRoutineResource(
        String treatmentPlanId,
        Integer currentOrder,
        String name,
        Integer newOrder,
        RoutineScheduleResource schedule,
        List<ExerciseSeriesRequestResource> exerciseSeries
) {
}
