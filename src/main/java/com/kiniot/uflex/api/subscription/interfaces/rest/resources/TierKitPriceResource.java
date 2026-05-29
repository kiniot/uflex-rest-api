package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import java.math.BigDecimal;

public record TierKitPriceResource(
        String currency,
        BigDecimal unitAmount
) {
}
