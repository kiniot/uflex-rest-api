package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TierPriceId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {
    public TierPriceId {
        if (id == null) {
            throw new IllegalArgumentException("Tier price ID cannot be null");
        }
    }

    public TierPriceId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}
