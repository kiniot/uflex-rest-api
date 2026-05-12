package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record ClinicId(
        @Column(columnDefinition = "UUID", nullable = false)
        UUID clinicId
) implements Serializable {
    public ClinicId {
        if (clinicId == null) {
            throw new IllegalArgumentException("Clinic ID cannot be null");
        }
    }

    public ClinicId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}