package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

public record TierCatalogPrice(
        BillingPeriod billingPeriod,
        Money price
) {
    public TierCatalogPrice {
        if (billingPeriod == null)
            throw new IllegalArgumentException("Billing period cannot be null");
        if (price == null)
            throw new IllegalArgumentException("Price cannot be null");
    }
}
