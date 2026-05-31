package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public record TreatmentPlanPeriod(
        @Column(name = "starts_at", nullable = false)
        LocalDate startsAt,
        @Column(name = "ends_at", nullable = false)
        LocalDate endsAt
) {
    public TreatmentPlanPeriod {
        if (startsAt == null) {
            throw new IllegalArgumentException("Treatment plan startsAt cannot be null");
        }
        if (endsAt == null) {
            throw new IllegalArgumentException("Treatment plan endsAt cannot be null");
        }
        if (endsAt.isBefore(startsAt)) {
            throw new IllegalArgumentException("Treatment plan endsAt cannot be before startsAt");
        }
    }
}
