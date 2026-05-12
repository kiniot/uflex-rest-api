package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record ExerciseDuration(
        @Column(name = "duration_seconds", nullable = false)
        Integer seconds
) {
    public ExerciseDuration {
        if (seconds == null) {
            throw new IllegalArgumentException("Duration cannot be null");
        }
        if (seconds <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }
    }
}
