package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public record PatientId(
        @Column(columnDefinition = "UUID", nullable = false)
        UUID id
) implements Serializable {

    public PatientId {
        Objects.requireNonNull(id, "patientId must not be null");
    }

    public static PatientId of(UUID id) {
        Objects.requireNonNull(id, "patientId must not be null");
        return new PatientId(id);
    }

    public static PatientId fromNullable(UUID id) {
        return id == null ? null : new PatientId(id);
    }

    public static String toStringOrNull(PatientId vo) {
        return vo == null ? null : vo.id().toString();
    }
}
