package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

public record CheckoutSessionResource(
        String subscriptionId,
        String status,
        String checkoutUrl
) {
}
