package com.kiniot.uflex.api.iam.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

/**
 * Value Object representing a unique User identifier.
 * <p>
 * This class encapsulates a UUID v7 (time-based epoch) identifier, ensuring uniqueness
 * and proper sorting across distributed systems. As an embedded value object, it serves
 * as the primary key for the User entity while maintaining domain-driven design principles.
 * <p>
 * The UUID is immutable and non-null by design, enforcing invariants at construction time.
 *
 * @param id UUID v7 that uniquely identifies a user, automatically generated if not explicitly provided
 * @see Generators#timeBasedEpochGenerator()
 */
@Embeddable
public record UserId (
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {
    public UserId {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    /**
     * Generates a new random UserId with a time-based epoch UUID v7.
     * <p>
     * UUID v7 provides temporal ordering while maintaining randomness, making it ideal
     * for distributed systems where identifiers are generated independently on multiple nodes.
     * This approach improves database performance by maintaining insertion order in indices.
     *
     * @see com.fasterxml.uuid.Generators#timeBasedEpochGenerator()
     */
    public UserId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}