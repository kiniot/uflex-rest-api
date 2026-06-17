package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import java.util.Objects;

public record SubscriptionCheckoutResult(
        SubscriptionId subscriptionId,
        SubscriptionStatus status,
        String checkoutUrl
) {
    public SubscriptionCheckoutResult {
        Objects.requireNonNull(subscriptionId, "Subscription ID cannot be null");
        Objects.requireNonNull(status, "Subscription status cannot be null");
        if (checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new IllegalArgumentException("Checkout URL cannot be null or empty");
        }
    }
}
