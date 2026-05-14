package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import java.util.List;

public record RoutineResource(
        String id,
        String name,
        Integer order,
        RoutineScheduleResource schedule,
        List<ExerciseSeriesResource> exerciseSeries
) {
}
