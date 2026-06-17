package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import java.math.BigDecimal;

public record TierPriceResource(
        String billingPeriod,
        String currency,
        BigDecimal amount
) {
}
