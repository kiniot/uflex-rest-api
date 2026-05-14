package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record RoutineName(
        String name
) {
    public RoutineName {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Routine name cannot be null or blank");
        }
        name = name.trim();
    }
}
