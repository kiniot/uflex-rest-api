package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChangeSubscriptionPlanCommand(
        UUID subscriptionId,
        UUID newPlanId,
        BillingCycle newBillingCycle,
        OffsetDateTime effectiveAt
) {
}
