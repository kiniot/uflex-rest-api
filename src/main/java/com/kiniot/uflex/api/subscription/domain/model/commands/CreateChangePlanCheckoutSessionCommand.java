package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId;

public record CreateChangePlanCheckoutSessionCommand(
        SubscriptionId subscriptionId,
        SubscriptionPlanId newPlanId,
        BillingCycle newBillingCycle,
        UserId userId
) {
}
