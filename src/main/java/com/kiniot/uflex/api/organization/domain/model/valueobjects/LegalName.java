package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record LegalName(
        @Column(nullable = false, length = 200)
        String legalName
) {
    public LegalName {
        if (legalName == null || legalName.isBlank()) {
            throw new IllegalArgumentException("Legal name cannot be null or blank");
        }
        if (legalName.length() > 200) {
            throw new IllegalArgumentException("Legal name cannot exceed 200 characters");
        }
    }

    public LegalName() {
        this("");
    }
}