package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import java.util.List;

public record UpdateRoutineResource(
        String name,
        Integer newOrder,
        RoutineScheduleResource schedule,
        List<ExerciseSeriesRequestResource> exerciseSeries
) {
}
