package com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<SubscriptionPlan, SubscriptionPlanId> {
    List<SubscriptionPlan> findAllByActiveTrue();
    Optional<SubscriptionPlan> findByCode(String code);
    boolean existsByCode(String code);
}
