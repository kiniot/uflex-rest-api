package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import java.util.Objects;

public enum RoutineStatus {
    Pending, InProgress, Completed;

    public static RoutineStatus of(String value) {
        Objects.requireNonNull(value, "routineStatus must not be null");
        return valueOf(value);
    }

    public static RoutineStatus fromNullable(String value) {
        return (value == null || value.isBlank()) ? null : of(value);
    }

    public static String toStringOrNull(RoutineStatus status) {
        return status == null ? null : status.name();
    }
}
