package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

/**
 * Value Object representing a unique TreatmentPlan identifier.

 * @param id UUID v7 that uniquely identifies a treatment plan, automatically generated if not explicitly provided
 * @see Generators#timeBasedEpochGenerator()
 */
@Embeddable
public record TreatmentPlanId (
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {
    public TreatmentPlanId {
        if (id == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
    }

    /**
     * Generates a new random TreatmentPlanId with a time-based epoch UUID v7.
     *
     * @see com.fasterxml.uuid.Generators#timeBasedEpochGenerator()
     */
    public TreatmentPlanId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}