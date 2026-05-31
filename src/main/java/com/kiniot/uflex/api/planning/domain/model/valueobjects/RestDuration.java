package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record RestDuration(
        @Column(name = "rest_duration_seconds", nullable = false)
        Integer seconds
) {
    public RestDuration {
        if (seconds == null) {
            throw new IllegalArgumentException("Rest duration cannot be null");
        }
        if (seconds < 0) {
            throw new IllegalArgumentException("Rest duration cannot be negative");
        }
    }
}
