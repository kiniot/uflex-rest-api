package com.kiniot.uflex.api.therapy.domain.exceptions;

public class TherapySessionAlreadyFinalizedException extends RuntimeException {

    private TherapySessionAlreadyFinalizedException(String message) {
        super(message);
    }

    public static TherapySessionAlreadyFinalizedException forSession(String sessionId) {
        return new TherapySessionAlreadyFinalizedException(
                "TherapySession with ID %s is already in a terminal state".formatted(sessionId));
    }
}
