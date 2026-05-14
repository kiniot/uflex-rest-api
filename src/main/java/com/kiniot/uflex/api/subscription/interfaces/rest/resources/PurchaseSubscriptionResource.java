package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;

import java.util.UUID;

public record PurchaseSubscriptionResource(
        UUID clinicId,
        UUID planId,
        BillingCycle billingCycle,
        String paymentToken
) {
}
