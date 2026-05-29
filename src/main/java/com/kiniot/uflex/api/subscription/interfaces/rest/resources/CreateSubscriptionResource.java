package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CreateSubscriptionResource(
        @Schema(description = "Subscription tier identifier")
        String tierId,
        @Schema(description = "Billing period for the subscription tier")
        String billingPeriod,
        @Schema(
                description = "Contracted subscription amount as a numeric decimal with exactly 2 fractional digits"
        )
        BigDecimal amount,
        @Schema(description = "ISO currency code")
        String currency,
        @Schema(description = "Total kits requested for the subscription")
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
