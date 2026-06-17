package com.kiniot.uflex.api.therapy.domain.exceptions;

public class IoTSensorsNotPlacedException extends RuntimeException {

    private IoTSensorsNotPlacedException(String message) {
        super(message);
    }

    public static IoTSensorsNotPlacedException forDevice(String deviceId) {
        return new IoTSensorsNotPlacedException(
                "IoT sensors are not placed for device %s".formatted(deviceId));
    }
}
