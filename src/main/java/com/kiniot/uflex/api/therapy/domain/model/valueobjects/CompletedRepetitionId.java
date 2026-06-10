package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public record CompletedRepetitionId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {

    public CompletedRepetitionId {
        Objects.requireNonNull(id, "completedRepetitionId must not be null");
    }

    public CompletedRepetitionId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }

    public static CompletedRepetitionId of(UUID id) {
        Objects.requireNonNull(id, "completedRepetitionId must not be null");
        return new CompletedRepetitionId(id);
    }

    public static String toStringOrNull(CompletedRepetitionId vo) {
        return vo == null ? null : vo.id().toString();
    }
}
