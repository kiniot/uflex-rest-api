package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record ExerciseSeriesOrder(
        @Column(nullable = false)
        Integer value
) {
    public ExerciseSeriesOrder {
        if (value == null) {
            throw new IllegalArgumentException("Exercise series order cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("Exercise series order must be greater than zero");
        }
    }
}
