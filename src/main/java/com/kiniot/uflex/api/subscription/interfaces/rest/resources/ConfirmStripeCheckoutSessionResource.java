package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ConfirmStripeCheckoutSessionResource(
        @JsonAlias("session_id")
        String sessionId
) {
}
