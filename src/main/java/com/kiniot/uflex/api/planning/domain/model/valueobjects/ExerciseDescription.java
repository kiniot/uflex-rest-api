package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ExerciseDescription(
        String description
) {
    public ExerciseDescription {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Exercise description cannot be null or blank");
        }
        description = description.trim();
    }
}
