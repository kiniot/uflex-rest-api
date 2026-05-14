package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId;

import java.time.OffsetDateTime;

public record CompleteCheckoutSessionPaymentCommand(
        ClinicId clinicId,
        SubscriptionPlanId planId,
        BillingCycle billingCycle,
        PaymentReference paymentReference,
        OffsetDateTime currentPeriodStart,
        OffsetDateTime currentPeriodEnd
) {
}
