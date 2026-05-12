package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record RoutineOrder(
        @Column(nullable = false)
        Integer value
) {
    public RoutineOrder {
        if (value == null) {
            throw new IllegalArgumentException("Routine order cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("Routine order must be greater than zero");
        }
    }
}
