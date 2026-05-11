package com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment;

public record CheckoutSessionResult(
        String sessionId,
        String checkoutUrl
) {
}
