package com.kiniot.uflex.api.device.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record BatteryLevel(
        @Column(nullable = false)
        Integer percentage
) {
    public BatteryLevel {
        if (percentage == null || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Battery level must be between 0 and 100");
        }
    }

    public BatteryLevel() {
        this(100);
    }

    public boolean isLow() {
        return percentage < 20;
    }
}