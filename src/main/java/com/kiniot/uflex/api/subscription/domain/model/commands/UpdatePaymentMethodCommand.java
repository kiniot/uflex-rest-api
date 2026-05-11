package com.kiniot.uflex.api.subscription.domain.model.commands;

import java.util.UUID;

public record UpdatePaymentMethodCommand(
        UUID subscriptionId,
        String paymentToken,
        String last4,
        String expiresOn
) {
}
