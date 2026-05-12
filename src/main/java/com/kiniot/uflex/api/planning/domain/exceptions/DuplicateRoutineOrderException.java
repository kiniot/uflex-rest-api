package com.kiniot.uflex.api.planning.domain.exceptions;

public class DuplicateRoutineOrderException extends RuntimeException {
    public DuplicateRoutineOrderException(Integer order) {
        super("Routine order %s already exists within the treatment plan".formatted(order));
    }
}
