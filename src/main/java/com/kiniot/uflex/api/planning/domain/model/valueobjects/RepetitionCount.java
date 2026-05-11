package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record RepetitionCount(
        @Column(nullable = false)
        Integer value
) {
    public RepetitionCount {
        if (value == null) {
            throw new IllegalArgumentException("Repetition count cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("Repetition count must be greater than zero");
        }
    }
}
