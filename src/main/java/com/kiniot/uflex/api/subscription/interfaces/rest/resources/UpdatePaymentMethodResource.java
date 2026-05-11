package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

public record UpdatePaymentMethodResource(
        String paymentToken,
        String last4,
        String expiresOn
) {
}
