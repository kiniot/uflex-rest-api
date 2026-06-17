package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public record RoutineId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {

    public RoutineId {
        Objects.requireNonNull(id, "routineId must not be null");
    }

    public RoutineId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }

    public static RoutineId of(UUID id) {
        Objects.requireNonNull(id, "routineId must not be null");
        return new RoutineId(id);
    }

    public static RoutineId fromNullable(UUID id) {
        return id == null ? null : new RoutineId(id);
    }

    public static String toStringOrNull(RoutineId vo) {
        return vo == null ? null : vo.id().toString();
    }
}
