package com.kiniot.uflex.api.subscription.domain.services;

import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.commands.GetCurrentSubscriptionQuery;

import java.util.Optional;

public interface SubscriptionQueryService {
    Optional<Subscription> handle(GetCurrentSubscriptionQuery query);
}
