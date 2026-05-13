package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CurrentSubscriptionResource(
        UUID id,
        UUID clinicId,
        UUID planId,
        String planName,
        SubscriptionStatus status,
        BillingCycle billingCycle,
        BigDecimal amount,
        String currency,
        OffsetDateTime currentPeriodStart,
        OffsetDateTime currentPeriodEnd,
        OffsetDateTime nextBillingDate,
        PaymentReferenceResource paymentReference
) {
}
