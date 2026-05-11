package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriptionResource(
        UUID id,
        UUID clinicId,
        PlanResource plan,
        SubscriptionStatus status,
        BillingCycle billingCycle,
        OffsetDateTime currentPeriodStart,
        OffsetDateTime currentPeriodEnd,
        OffsetDateTime nextBillingDate,
        OffsetDateTime trialUntil,
        PaymentReferenceResource paymentReference
) {
}
