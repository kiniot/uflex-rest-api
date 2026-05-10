package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record LegalName(
        @Column(nullable = false, length = 200)
        String value
) {
    public LegalName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Legal name cannot be null or blank");
        }
        if (value.length() > 200) {
            throw new IllegalArgumentException("Legal name cannot exceed 200 characters");
        }
    }

    public LegalName() {
        this("");
    }
}