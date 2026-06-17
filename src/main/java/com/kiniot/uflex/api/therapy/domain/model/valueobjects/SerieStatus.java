package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import java.util.Objects;

public enum SerieStatus {
    Pending, Started, Validated, Failed;

    public static SerieStatus of(String value) {
        Objects.requireNonNull(value, "serieStatus must not be null");
        return valueOf(value);
    }

    public static SerieStatus fromNullable(String value) {
        return (value == null || value.isBlank()) ? null : of(value);
    }

    public static String toStringOrNull(SerieStatus status) {
        return status == null ? null : status.name();
    }
}
