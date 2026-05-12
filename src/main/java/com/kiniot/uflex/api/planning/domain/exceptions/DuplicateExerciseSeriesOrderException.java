package com.kiniot.uflex.api.planning.domain.exceptions;

public class DuplicateExerciseSeriesOrderException extends RuntimeException {
    public DuplicateExerciseSeriesOrderException(Integer order) {
        super("Exercise series order %s already exists within the routine".formatted(order));
    }
}
