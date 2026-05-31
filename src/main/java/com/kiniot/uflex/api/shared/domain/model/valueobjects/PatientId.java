package com.kiniot.uflex.api.shared.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record PatientId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID patientId
) implements Serializable {
    public PatientId {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }

    public PatientId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}
