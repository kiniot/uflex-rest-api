package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record AngleThreshold(Double minAngle, Double maxAngle) {

    public AngleThreshold {
        Objects.requireNonNull(minAngle, "minAngle must not be null");
        Objects.requireNonNull(maxAngle, "maxAngle must not be null");
        if (minAngle < 0 || minAngle > 180)
            throw new IllegalArgumentException("minAngle must be between 0 and 180 degrees");
        if (maxAngle < 0 || maxAngle > 180)
            throw new IllegalArgumentException("maxAngle must be between 0 and 180 degrees");
        if (minAngle >= maxAngle)
            throw new IllegalArgumentException("minAngle must be less than maxAngle");
    }

    public static AngleThreshold of(Double minAngle, Double maxAngle) {
        return new AngleThreshold(minAngle, maxAngle);
    }

    public boolean contains(Double angle) {
        return angle != null && angle >= minAngle && angle <= maxAngle;
    }
}
