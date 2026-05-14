package com.kiniot.uflex.api.planning.interfaces.rest.resources;

public record CreateExerciseResource(
        String name,
        String description,
        String bodyPart
) {
}
