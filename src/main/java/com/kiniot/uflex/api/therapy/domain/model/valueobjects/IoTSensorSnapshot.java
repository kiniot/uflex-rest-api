package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record IoTSensorSnapshot(String deviceId, Boolean sensorsPlaced) {

    public IoTSensorSnapshot {
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        if (deviceId.isBlank()) throw new IllegalArgumentException("deviceId must not be blank");
        Objects.requireNonNull(sensorsPlaced, "sensorsPlaced must not be null");
    }

    public static IoTSensorSnapshot of(String deviceId, Boolean sensorsPlaced) {
        return new IoTSensorSnapshot(deviceId, sensorsPlaced);
    }
}
