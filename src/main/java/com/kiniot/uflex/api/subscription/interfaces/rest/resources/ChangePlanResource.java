package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChangePlanResource(
        UUID newPlanId,
        BillingCycle newBillingCycle,
        OffsetDateTime effectiveAt
) {
}
