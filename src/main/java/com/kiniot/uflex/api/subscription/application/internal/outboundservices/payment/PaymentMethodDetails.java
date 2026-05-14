package com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment;

public record PaymentMethodDetails(
        String brand,
        String last4,
        Integer expMonth,
        Integer expYear
) {
}
