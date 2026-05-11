package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PaymentReference(
        @Column(length = 40)
        String provider,

        @Column(length = 120)
        String providerTransactionId,

        @Column(length = 4)
        String last4,

        @Column(length = 20)
        String expiresOn
) {
    public static PaymentReference empty() {
        return new PaymentReference(null, null, null, null);
    }
}
