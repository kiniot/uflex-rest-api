package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ExerciseName(
        String name
) {
    public ExerciseName {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Exercise name cannot be null or blank");
        }
        name = name.trim();
    }
}
