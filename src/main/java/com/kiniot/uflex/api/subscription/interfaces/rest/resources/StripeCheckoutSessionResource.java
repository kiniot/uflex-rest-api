package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

public record StripeCheckoutSessionResource(
        String sessionId,
        String checkoutUrl
) {
}
