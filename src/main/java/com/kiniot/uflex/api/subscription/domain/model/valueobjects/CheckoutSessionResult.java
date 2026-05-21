package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

public record CheckoutSessionResult(
        String sessionId,
        String checkoutUrl
) {
    public CheckoutSessionResult {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Checkout session ID cannot be null or empty");
        }
        if (checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new IllegalArgumentException("Checkout URL cannot be null or empty");
        }
    }
}
