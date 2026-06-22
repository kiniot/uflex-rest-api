package com.kiniot.uflex.api.iam.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

/**
 * Value object representing the identity of an {@code EdgeServiceAccount}.
 * <p>
 * Stored as a UUID v7, matching the rest of the domain model.
 *
 * @param id the account UUID
 */
@Embeddable
public record EdgeServiceAccountId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {
    public EdgeServiceAccountId {
        if (id == null) {
            throw new IllegalArgumentException("EdgeServiceAccount ID cannot be null");
        }
    }

    public EdgeServiceAccountId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}
