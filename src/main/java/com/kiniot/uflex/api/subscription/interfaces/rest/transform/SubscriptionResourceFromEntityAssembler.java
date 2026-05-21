package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.SubscriptionResource;

import java.util.Objects;

public class SubscriptionResourceFromEntityAssembler {
    public static SubscriptionResource toResourceFromEntity(Subscription entity) {
        return new SubscriptionResource(
                Objects.requireNonNull(entity.getId()).id().toString(),
                entity.getSelection().tierId().id().toString(),
                entity.getSelection().billingPeriod().name(),
                entity.getContractedPrice().amount(),
                entity.getContractedPrice().currency().name(),
                entity.getStatus().name(),
                entity.getStartedAt().toString(),
                entity.getRenewsAt().toString(),
                entity.getEndsAt() != null ? entity.getEndsAt().toString() : null
        );
    }
}
