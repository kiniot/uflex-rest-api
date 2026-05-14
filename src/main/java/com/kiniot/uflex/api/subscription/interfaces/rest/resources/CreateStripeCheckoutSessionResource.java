package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import java.util.UUID;

public record CreateStripeCheckoutSessionResource(
        UUID planId,
        String billingCycle
) {
}
