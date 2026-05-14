package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.model;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.SubscriptionPaymentMethodDetails;

public record PaymentMethodDetails(
        String brand,
        String last4,
        Integer expMonth,
        Integer expYear
) implements SubscriptionPaymentMethodDetails {
}
