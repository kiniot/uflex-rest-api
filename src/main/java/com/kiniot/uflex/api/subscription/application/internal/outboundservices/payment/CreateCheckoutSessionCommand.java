package com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCheckoutSessionCommand(
        UUID clinicId,
        UUID planId,
        BillingCycle billingCycle,
        BigDecimal amount,
        String currency,
        String successUrl,
        String cancelUrl,
        String planName,
        String userId
) {
}
