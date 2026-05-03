package com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, TreatmentPlanId> {
}
