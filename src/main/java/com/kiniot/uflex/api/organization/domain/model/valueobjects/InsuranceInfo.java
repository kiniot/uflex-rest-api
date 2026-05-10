package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record InsuranceInfo(
        @Column(length = 150)
        String provider,
        @Column(length = 50)
        String policyNumber,
        @Column(length = 100)
        String coverage
) {
    public InsuranceInfo() {
        this(null, null, null);
    }
}