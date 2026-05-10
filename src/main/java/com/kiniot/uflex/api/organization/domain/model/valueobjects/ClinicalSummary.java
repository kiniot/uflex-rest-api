package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record ClinicalSummary(
        @Column(length = 500)
        String primaryDiagnosis,
        @Column(length = 300)
        String allergies,
        @Column(length = 500)
        String notes
) {
    public ClinicalSummary() {
        this(null, null, null);
    }
}