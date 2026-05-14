package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;

public record UpdatePaymentMethodCommand(
        SubscriptionId subscriptionId,
        String paymentToken,
        String last4,
        String expiresOn
) {
}
