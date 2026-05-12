package com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, TreatmentPlanId> {

    @EntityGraph(attributePaths = {"routines", "routines.exerciseSeries"})
    Optional<TreatmentPlan> findWithRoutinesAndExerciseSeriesById(TreatmentPlanId id);

    List<TreatmentPlan> findAllByClinicId(ClinicId clinicId);

    @EntityGraph(attributePaths = {"routines", "routines.exerciseSeries"})
    List<TreatmentPlan> findAllWithRoutinesAndExerciseSeriesByClinicId(ClinicId clinicId);

    boolean existsByClinicIdAndPlanName(ClinicId clinicId, PlanName planName);
}
