package com.kiniot.uflex.api.subscription.application.internal.commandservices;

import com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionPricingService;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionPricingServiceImpl implements SubscriptionPricingService {
    @Override
    public Money priceFor(SubscriptionPlan plan, BillingCycle billingCycle) {
        return billingCycle == BillingCycle.YEARLY ? plan.getYearlyPrice() : plan.getMonthlyPrice();
    }

    @Override
    public Money amountForPlanChange(SubscriptionPlan plan, BillingCycle billingCycle) {
        // TODO: replace this simple full-cycle charge with prorated billing when billing rules are defined.
        return priceFor(plan, billingCycle);
    }
}
