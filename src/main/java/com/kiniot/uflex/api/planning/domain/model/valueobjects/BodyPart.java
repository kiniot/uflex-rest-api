package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record BodyPart(
        @Column(nullable = false, length = 40)
        String name
) {
    public BodyPart {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Body part cannot be null or blank");
        }
        name = name.trim();
    }
}
