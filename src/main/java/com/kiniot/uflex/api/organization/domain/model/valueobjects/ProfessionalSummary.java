package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record ProfessionalSummary(
        @Column(length = 1000)
        String summary
) {
    public ProfessionalSummary {
        if (summary != null && summary.length() > 1000) {
            throw new IllegalArgumentException("Professional summary cannot exceed 1000 characters");
        }
    }

    public ProfessionalSummary() {
        this(null);
    }
}