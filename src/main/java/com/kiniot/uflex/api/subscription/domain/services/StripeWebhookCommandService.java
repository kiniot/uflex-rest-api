package com.kiniot.uflex.api.subscription.domain.services;

public interface StripeWebhookCommandService {
    void handleCheckoutCompleted(String checkoutSessionId);
    void handleCheckoutExpired(String checkoutSessionId);
}
