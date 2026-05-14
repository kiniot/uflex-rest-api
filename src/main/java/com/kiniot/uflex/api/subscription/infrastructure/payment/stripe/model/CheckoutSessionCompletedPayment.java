package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.model;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CompletedCheckoutPayment;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckoutSessionCompletedPayment(
        String sessionId,
        String paymentIntentId,
        String paymentStatus,
        String status,
        UUID clinicId,
        UUID planId,
        BillingCycle billingCycle,
        BigDecimal amountTotal,
        String currency,
        PaymentReference paymentReference,
        OffsetDateTime currentPeriodStart,
        OffsetDateTime currentPeriodEnd
) implements CompletedCheckoutPayment {
}
