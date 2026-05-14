package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

public record PaymentReferenceResource(
        String provider,
        String providerTransactionId,
        String providerCheckoutSessionId,
        String providerCustomerId,
        String providerSubscriptionId,
        String last4,
        String expiresOn
) {
}
