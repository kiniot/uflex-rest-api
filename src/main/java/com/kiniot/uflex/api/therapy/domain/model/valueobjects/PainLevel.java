package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record PainLevel(Integer value) {

    public PainLevel {
        Objects.requireNonNull(value, "painLevel must not be null");
        if (value < 0 || value > 10)
            throw new IllegalArgumentException("Pain level must be between 0 and 10");
    }

    public static PainLevel of(Integer value) {
        return new PainLevel(value);
    }
}
