package com.kiniot.uflex.api.therapy.domain.exceptions;

public class IoTSensorsNotPlacedException extends RuntimeException {

    private IoTSensorsNotPlacedException(String message) {
        super(message);
    }

    public static IoTSensorsNotPlacedException forSession(String sessionId) {
        return new IoTSensorsNotPlacedException(
                "IoT sensors are not placed for session %s".formatted(sessionId));
    }
}
