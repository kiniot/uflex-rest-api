package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;

import java.util.UUID;

public record PurchaseSubscriptionPlanCommand(
        UUID clinicId,
        UUID planId,
        BillingCycle billingCycle,
        String paymentToken
) {
}
