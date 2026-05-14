package com.kiniot.uflex.api.subscription.domain.model.queries;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId;

public record GetPlanByIdQuery(SubscriptionPlanId planId) {
}
