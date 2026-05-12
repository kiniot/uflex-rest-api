package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record UserId (
        @Column(columnDefinition = "UUID", nullable = false)
        UUID userId
) implements Serializable {
    public UserId {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    public UserId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}