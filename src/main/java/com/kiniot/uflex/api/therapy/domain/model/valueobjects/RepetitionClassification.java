package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import java.util.Objects;

public enum RepetitionClassification {
    Good, Incomplete, Unsafe;

    public static RepetitionClassification of(String value) {
        Objects.requireNonNull(value, "classification must not be null");
        return valueOf(value);
    }

    public static RepetitionClassification fromNullable(String value) {
        return (value == null || value.isBlank()) ? null : of(value);
    }

    public static String toStringOrNull(RepetitionClassification classification) {
        return classification == null ? null : classification.name();
    }
}
