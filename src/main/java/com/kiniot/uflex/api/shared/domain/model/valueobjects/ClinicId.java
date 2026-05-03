package com.kiniot.uflex.api.shared.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

/**
 * Value object representing the clinic identifier.
 * <p>
 * It plays the same role as {@code TenantId}, but uses a more ubiquitous term
 * shared across bounded contexts.
 */
@Embeddable
public record ClinicId(
        @Column(columnDefinition = "UUID")
        UUID clinicId
) {
    public ClinicId() {
        this(null);
    }

    public ClinicId {
        if (clinicId != null && clinicId.equals(new UUID(0L, 0L))) {
            throw new IllegalArgumentException("Clinic ID cannot be null or zero");
        }
    }
}