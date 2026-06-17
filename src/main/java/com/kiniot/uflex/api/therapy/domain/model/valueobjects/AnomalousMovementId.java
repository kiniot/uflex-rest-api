package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public record AnomalousMovementId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {

    public AnomalousMovementId {
        Objects.requireNonNull(id, "anomalousMovementId must not be null");
    }

    public AnomalousMovementId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }

    public static AnomalousMovementId of(UUID id) {
        Objects.requireNonNull(id, "anomalousMovementId must not be null");
        return new AnomalousMovementId(id);
    }

    public static String toStringOrNull(AnomalousMovementId vo) {
        return vo == null ? null : vo.id().toString();
    }
}
