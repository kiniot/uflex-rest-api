package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.model;

public record CheckoutSessionResult(
        String sessionId,
        String checkoutUrl
) implements com.kiniot.uflex.api.subscription.domain.services.results.CheckoutSessionResult {
}
