package com.kiniot.uflex.api.subscription.application.internal.queryservices;

import com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetPlanByIdQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetPlanListQuery;
import com.kiniot.uflex.api.subscription.domain.services.PlanQueryService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanQueryServiceImpl implements PlanQueryService {
    private final PlanRepository planRepository;

    public PlanQueryServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public List<SubscriptionPlan> handle(GetPlanListQuery query) {
        return planRepository.findAllByActiveTrue();
    }

    @Override
    public Optional<SubscriptionPlan> handle(GetPlanByIdQuery query) {
        return planRepository.findById(query.planId()).filter(SubscriptionPlan::isActive);
    }
}
