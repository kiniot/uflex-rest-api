package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import java.util.UUID;

public record ChangePlanCheckoutSessionResource(
        UUID planId,
        String billingCycle
) {
}
