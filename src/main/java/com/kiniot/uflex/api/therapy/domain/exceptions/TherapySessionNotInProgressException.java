package com.kiniot.uflex.api.therapy.domain.exceptions;

import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;

public class TherapySessionNotInProgressException extends RuntimeException {

    private TherapySessionNotInProgressException(String message) {
        super(message);
    }

    public static TherapySessionNotInProgressException forSession(String sessionId, SessionStatus currentStatus) {
        return new TherapySessionNotInProgressException(
                "TherapySession %s is in status %s; the operation requires InProgress".formatted(sessionId, currentStatus));
    }
}
