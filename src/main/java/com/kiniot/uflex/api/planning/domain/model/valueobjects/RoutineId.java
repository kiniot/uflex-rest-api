package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record RoutineId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) {
    public RoutineId {
        if (id == null) {
            throw new IllegalArgumentException("Exercise ID cannot be null");
        }
    }
    public RoutineId() {
        this((Generators.timeBasedEpochGenerator().generate()));
    }
}
