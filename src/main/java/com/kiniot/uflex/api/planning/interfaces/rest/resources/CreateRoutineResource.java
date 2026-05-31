package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import java.util.List;

public record CreateRoutineResource(
        String name,
        Integer order,
        RoutineScheduleResource schedule,
        List<ExerciseSeriesRequestResource> exerciseSeries
) {
}
