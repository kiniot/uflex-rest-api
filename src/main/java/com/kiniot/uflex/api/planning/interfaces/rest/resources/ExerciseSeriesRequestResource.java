package com.kiniot.uflex.api.planning.interfaces.rest.resources;

public record ExerciseSeriesRequestResource(
        Integer order,
        String exerciseId,
        Integer rangeOfMotionDegrees,
        Integer repetitions,
        Integer durationSeconds,
        Integer restDurationSeconds
) {
}
