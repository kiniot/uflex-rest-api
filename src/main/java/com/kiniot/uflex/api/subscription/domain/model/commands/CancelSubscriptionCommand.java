package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;

public record CancelSubscriptionCommand(
        SubscriptionId subscriptionId,
        String reason
) {
}
