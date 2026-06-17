package com.kiniot.uflex.api.therapy.domain.exceptions;

public class HardwareNotReadyException extends RuntimeException {

    private HardwareNotReadyException(String message) {
        super(message);
    }

    public static HardwareNotReadyException forSession(String sessionId) {
        return new HardwareNotReadyException(
                "TherapySession with ID %s cannot start: hardware is not in Ready state".formatted(sessionId));
    }
}
