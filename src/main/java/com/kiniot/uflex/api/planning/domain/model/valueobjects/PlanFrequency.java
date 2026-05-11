package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public record PlanFrequency(
        @Column(nullable = false)
        Integer occurrences,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 10)
        FrequencyUnit unit
) {
    public PlanFrequency {
        if (occurrences == null) {
            throw new IllegalArgumentException("Occurrences cannot be null");
        }
        if (occurrences <= 0) {
            throw new IllegalArgumentException("Occurrences must be greater than zero");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Frequency unit cannot be null");
        }
    }
}
