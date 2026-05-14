package com.kiniot.uflex.api.subscription.application.internal.commandservices;

import com.kiniot.uflex.api.subscription.domain.model.commands.CreatePlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.DeactivatePlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan;
import com.kiniot.uflex.api.subscription.domain.services.PlanCommandService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlanCommandServiceImpl implements PlanCommandService {
    private final PlanRepository planRepository;

    public PlanCommandServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public Optional<SubscriptionPlan> handle(CreatePlanCommand command) {
        return planRepository.findByCode(command.code())
                .or(() -> Optional.of(planRepository.save(new SubscriptionPlan(command))));
    }

    @Override
    public void handle(DeactivatePlanCommand command) {
        planRepository.findById(command.planId()).ifPresent(plan -> {
            plan.deactivate();
            planRepository.save(plan);
        });
    }
}
