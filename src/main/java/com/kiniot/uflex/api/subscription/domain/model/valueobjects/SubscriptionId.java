package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record SubscriptionId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {
    public SubscriptionId {
        if (id == null) {
            throw new IllegalArgumentException("Subscription ID cannot be null");
        }
    }

    public SubscriptionId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}
