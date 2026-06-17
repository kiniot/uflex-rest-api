package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TierId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {
    public TierId {
        if (id == null) {
            throw new IllegalArgumentException("Tier ID cannot be null");
        }
    }

    public TierId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}
