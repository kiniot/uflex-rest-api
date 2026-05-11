package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record SubscriptionPlanId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {
    public SubscriptionPlanId {
        if (id == null) {
            throw new IllegalArgumentException("Subscription plan ID cannot be null");
        }
    }

    public SubscriptionPlanId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}
