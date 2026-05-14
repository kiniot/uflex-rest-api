package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

public record StripePaymentMethodResource(
        String brand,
        String last4,
        Integer expMonth,
        Integer expYear
) {
}
