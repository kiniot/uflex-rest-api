package com.kiniot.uflex.api.planning.interfaces.rest.resources;

public record UpdateExerciseResource(
        String name,
        String description,
        String bodyPart
) {
}
