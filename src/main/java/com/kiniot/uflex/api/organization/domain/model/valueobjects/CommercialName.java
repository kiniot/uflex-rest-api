package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record CommercialName(
        @Column(nullable = false, length = 150)
        String commercialName
) {
    public CommercialName {
        if (commercialName == null || commercialName.isBlank()) {
            throw new IllegalArgumentException("Commercial name cannot be null or blank");
        }
        if (commercialName.length() > 150) {
            throw new IllegalArgumentException("Commercial name cannot exceed 150 characters");
        }
    }

    public CommercialName() {
        this("");
    }
}