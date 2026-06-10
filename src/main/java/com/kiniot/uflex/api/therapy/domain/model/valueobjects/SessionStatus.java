package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import java.util.Objects;

public enum SessionStatus {
    Pending, Ready, InProgress, Completed, Cancelled;

    public static SessionStatus of(String value) {
        Objects.requireNonNull(value, "sessionStatus must not be null");
        return valueOf(value);
    }

    public static SessionStatus fromNullable(String value) {
        return (value == null || value.isBlank()) ? null : of(value);
    }

    public static String toStringOrNull(SessionStatus status) {
        return status == null ? null : status.name();
    }
}
