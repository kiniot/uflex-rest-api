package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

public record SubscriptionSelection(
        TierId tierId,
        BillingPeriod billingPeriod
) {
    public SubscriptionSelection {
        if (tierId == null) {
            throw new IllegalArgumentException("Tier ID cannot be null");
        }
        if (billingPeriod == null) {
            throw new IllegalArgumentException("Billing period cannot be null");
        }
    }
}
