package com.kiniot.uflex.api.planning.domain.exceptions;

public class RoutineWithOrderNotFoundException extends RuntimeException {
    public RoutineWithOrderNotFoundException(Integer order) {
        super("Routine with order %s not found".formatted(order));
    }
}
