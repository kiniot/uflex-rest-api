package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import java.util.Objects;

public enum AlertType {
    ExcessiveMovement, AnomalousMovement;

    public static AlertType of(String value) {
        Objects.requireNonNull(value, "alertType must not be null");
        return valueOf(value);
    }

    public static AlertType fromNullable(String value) {
        return (value == null || value.isBlank()) ? null : of(value);
    }

    public static String toStringOrNull(AlertType alertType) {
        return alertType == null ? null : alertType.name();
    }
}
