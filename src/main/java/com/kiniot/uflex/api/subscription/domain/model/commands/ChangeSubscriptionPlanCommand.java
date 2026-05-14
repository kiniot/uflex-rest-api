package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId;

import java.time.OffsetDateTime;

public record ChangeSubscriptionPlanCommand(
        SubscriptionId subscriptionId,
        SubscriptionPlanId newPlanId,
        BillingCycle newBillingCycle,
        OffsetDateTime effectiveAt
) {
}
