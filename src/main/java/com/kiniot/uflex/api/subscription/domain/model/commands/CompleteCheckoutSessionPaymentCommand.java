package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CompleteCheckoutSessionPaymentCommand(
        UUID clinicId,
        UUID planId,
        BillingCycle billingCycle,
        PaymentReference paymentReference,
        OffsetDateTime currentPeriodStart,
        OffsetDateTime currentPeriodEnd
) {
}
