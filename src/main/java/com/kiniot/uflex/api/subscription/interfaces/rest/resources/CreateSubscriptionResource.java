package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import java.math.BigDecimal;

public record CreateSubscriptionResource(
        String tierId,
        String billingPeriod,
        BigDecimal amount,
        String currency,
        Integer requestedTotalKits
) {
    public CreateSubscriptionResource {
        if (tierId == null || tierId.isBlank())
            throw new IllegalArgumentException("Tier ID can not be null or empty");
        if (billingPeriod == null || billingPeriod.isBlank())
            throw new IllegalArgumentException("Billing period can not be null or empty");
        if (amount == null || amount.signum() < 0)
            throw new IllegalArgumentException("Price amount can not be negative");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Currency can not be null or empty");
        if (requestedTotalKits == null || requestedTotalKits < 0)
            throw new IllegalArgumentException("Requested total kits can not be null or negative");
    }
}
