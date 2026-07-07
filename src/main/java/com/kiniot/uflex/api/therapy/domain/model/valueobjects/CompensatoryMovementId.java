package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public record CompensatoryMovementId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {

    public CompensatoryMovementId {
        Objects.requireNonNull(id, "compensatoryMovementId must not be null");
    }

    public CompensatoryMovementId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }

    public static CompensatoryMovementId of(UUID id) {
        Objects.requireNonNull(id, "compensatoryMovementId must not be null");
        return new CompensatoryMovementId(id);
    }

    public static String toStringOrNull(CompensatoryMovementId vo) {
        return vo == null ? null : vo.id().toString();
    }
}
