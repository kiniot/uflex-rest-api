package com.kiniot.uflex.api.therapy.domain.exceptions;

public class RoutineNotCompletedException extends RuntimeException {

    private RoutineNotCompletedException(String message) {
        super(message);
    }

    public static RoutineNotCompletedException forSession(String sessionId) {
        return new RoutineNotCompletedException(
                "TherapySession with ID %s cannot be finalized: the routine has not been completed"
                        .formatted(sessionId));
    }
}
