package com.kiniot.uflex.api.shared.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record PhysiotherapistId(
        @Column(columnDefinition = "UUID", nullable = true)
        UUID physiotherapistId
) implements Serializable {
    public PhysiotherapistId {
        if (physiotherapistId == null) {
            throw new IllegalArgumentException("Physiotherapist ID cannot be null");
        }
    }

    public PhysiotherapistId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}