package com.kiniot.uflex.api.therapy.domain.exceptions;

public class TherapySessionStillInProgressException extends RuntimeException {

    private TherapySessionStillInProgressException(String message) {
        super(message);
    }

    public static TherapySessionStillInProgressException forSession(String sessionId) {
        return new TherapySessionStillInProgressException(
                "Summary is not available for TherapySession with ID %s: session is still in progress"
                        .formatted(sessionId));
    }
}
