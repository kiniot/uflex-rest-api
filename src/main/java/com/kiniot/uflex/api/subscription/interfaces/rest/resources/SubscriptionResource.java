package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import java.math.BigDecimal;

public record SubscriptionResource(
        String id,
        String tierId,
        String billingPeriod,
        BigDecimal amount,
        String currency,
        String status,
        String startedAt,
        String renewsAt,
        String endsAt
) {
}
