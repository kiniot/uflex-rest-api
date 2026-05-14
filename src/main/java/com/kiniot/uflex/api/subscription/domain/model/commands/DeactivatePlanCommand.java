package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId;

public record DeactivatePlanCommand(SubscriptionPlanId planId) {
}
