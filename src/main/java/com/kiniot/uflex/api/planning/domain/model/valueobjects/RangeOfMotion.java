package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record RangeOfMotion(
        Integer degrees
) {
    public RangeOfMotion {
        if (degrees == null) {
            throw new IllegalArgumentException("Degrees cannot be null");
        }
        if (degrees < 0) {
            throw new IllegalArgumentException("Degrees cannot be negative");
        }
    }
}
