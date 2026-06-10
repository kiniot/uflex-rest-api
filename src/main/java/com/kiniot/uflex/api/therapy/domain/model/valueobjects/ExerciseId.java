package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public record ExerciseId(
        @Column(columnDefinition = "UUID", nullable = false)
        UUID id
) implements Serializable {

    public ExerciseId {
        Objects.requireNonNull(id, "exerciseId must not be null");
    }

    public static ExerciseId of(UUID id) {
        Objects.requireNonNull(id, "exerciseId must not be null");
        return new ExerciseId(id);
    }

    public static ExerciseId fromNullable(UUID id) {
        return id == null ? null : new ExerciseId(id);
    }

    public static String toStringOrNull(ExerciseId vo) {
        return vo == null ? null : vo.id().toString();
    }
}
