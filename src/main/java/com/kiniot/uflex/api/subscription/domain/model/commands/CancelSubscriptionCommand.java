package com.kiniot.uflex.api.subscription.domain.model.commands;

import java.util.UUID;

public record CancelSubscriptionCommand(
        UUID subscriptionId,
        String reason
) {
}
