package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record ExerciseId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) {
    public ExerciseId {
        if (id == null) {
            throw new IllegalArgumentException("Exercise ID cannot be null");
        }
    }

    public ExerciseId() {
        this((Generators.timeBasedEpochGenerator().generate()));
    }
}
