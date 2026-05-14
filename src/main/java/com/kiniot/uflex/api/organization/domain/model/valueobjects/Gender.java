package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Gender(
        @Column(nullable = false, length = 20)
        String gender
) {
    public Gender {
        if (gender == null || gender.isBlank()) {
            throw new IllegalArgumentException("Gender cannot be null or blank");
        }
        String normalized = gender.trim().toUpperCase();
        if (!normalized.equals("MALE") && !normalized.equals("FEMALE") && !normalized.equals("OTHER")) {
            throw new IllegalArgumentException("Gender must be MALE, FEMALE, or OTHER");
        }
    }

    public Gender() {
        this("OTHER");
    }
}