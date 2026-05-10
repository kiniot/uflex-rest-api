package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record ClinicAdminId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {
    public ClinicAdminId {
        if (id == null) {
            throw new IllegalArgumentException("ClinicAdmin ID cannot be null");
        }
    }

    public ClinicAdminId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}