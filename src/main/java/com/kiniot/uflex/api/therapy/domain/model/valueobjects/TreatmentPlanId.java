package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public record TreatmentPlanId(
        @Column(columnDefinition = "UUID", nullable = false)
        UUID id
) implements Serializable {

    public TreatmentPlanId {
        Objects.requireNonNull(id, "treatmentPlanId must not be null");
    }

    public static TreatmentPlanId of(UUID id) {
        Objects.requireNonNull(id, "treatmentPlanId must not be null");
        return new TreatmentPlanId(id);
    }

    public static TreatmentPlanId fromNullable(UUID id) {
        return id == null ? null : new TreatmentPlanId(id);
    }

    public static String toStringOrNull(TreatmentPlanId vo) {
        return vo == null ? null : vo.id().toString();
    }
}
