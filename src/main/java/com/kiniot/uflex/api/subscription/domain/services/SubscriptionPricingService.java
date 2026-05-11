package com.kiniot.uflex.api.subscription.domain.services;

import com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;

public interface SubscriptionPricingService {
    Money priceFor(SubscriptionPlan plan, BillingCycle billingCycle);
    Money amountForPlanChange(SubscriptionPlan plan, BillingCycle billingCycle);
}
