package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId;

public record PurchaseSubscriptionPlanCommand(
        ClinicId clinicId,
        SubscriptionPlanId planId,
        BillingCycle billingCycle,
        String paymentToken
) {
}
