package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TreatmentPlanId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID treatmentPlanId
) implements Serializable {
    public TreatmentPlanId {
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
    }

    public TreatmentPlanId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}