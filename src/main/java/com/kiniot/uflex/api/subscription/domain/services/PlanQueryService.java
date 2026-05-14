package com.kiniot.uflex.api.subscription.domain.services;

import com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetPlanByIdQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetPlanListQuery;

import java.util.List;
import java.util.Optional;

public interface PlanQueryService {
    List<SubscriptionPlan> handle(GetPlanListQuery query);
    Optional<SubscriptionPlan> handle(GetPlanByIdQuery query);
}
