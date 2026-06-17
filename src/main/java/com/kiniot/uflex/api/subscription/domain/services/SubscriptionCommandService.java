package com.kiniot.uflex.api.subscription.domain.services;

import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionCheckoutResult;

import java.util.Optional;

public interface SubscriptionCommandService {
    Optional<SubscriptionCheckoutResult> handle(CreateSubscriptionCommand command);
}
