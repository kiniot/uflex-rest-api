package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import java.util.Objects;

public enum CompensatoryMovementType {
    ShoulderCompensation, TrunkCompensation;

    public static CompensatoryMovementType of(String value) {
        Objects.requireNonNull(value, "compensatoryMovementType must not be null");
        return valueOf(value);
    }

    public static CompensatoryMovementType fromNullable(String value) {
        return (value == null || value.isBlank()) ? null : of(value);
    }

    public static String toStringOrNull(CompensatoryMovementType type) {
        return type == null ? null : type.name();
    }
}
