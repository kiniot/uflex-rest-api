package com.kiniot.uflex.api.subscription.domain.services;

import com.kiniot.uflex.api.subscription.domain.model.commands.CreatePlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.DeactivatePlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan;

import java.util.Optional;

public interface PlanCommandService {
    Optional<SubscriptionPlan> handle(CreatePlanCommand command);
    void handle(DeactivatePlanCommand command);
}
