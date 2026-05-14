package com.kiniot.uflex.api.planning.domain.exceptions;

public class ExerciseWithIdNotFoundException extends RuntimeException {
    public ExerciseWithIdNotFoundException(String exerciseId) {
        super("Exercise with ID %s not found".formatted(exerciseId));
    }
}
