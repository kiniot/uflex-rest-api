package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record MedicalCondition(
        @Column(length = 500)
        String condition
) {
    public MedicalCondition {
        if (condition != null && condition.length() > 500) {
            throw new IllegalArgumentException("Medical condition cannot exceed 500 characters");
        }
    }

    public MedicalCondition() {
        this(null);
    }
}