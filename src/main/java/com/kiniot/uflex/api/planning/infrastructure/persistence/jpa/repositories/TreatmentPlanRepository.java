package com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanStatus;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, TreatmentPlanId>, JpaSpecificationExecutor<TreatmentPlan> {

    @EntityGraph(attributePaths = {"routines"})
    Optional<TreatmentPlan> findWithRoutinesAndExerciseSeriesById(TreatmentPlanId id);

    @EntityGraph(attributePaths = {"routines"})
    Optional<TreatmentPlan> findWithRoutinesAndExerciseSeriesByIdAndClinicId(TreatmentPlanId id, ClinicId clinicId);

    @EntityGraph(attributePaths = {"routines"})
    Optional<TreatmentPlan> findWithRoutinesAndExerciseSeriesByIdAndClinicIdAndPatientId(
            TreatmentPlanId id,
            ClinicId clinicId,
            PatientId patientId
    );

    List<TreatmentPlan> findAllByClinicId(ClinicId clinicId);

    @EntityGraph(attributePaths = {"routines"})
    List<TreatmentPlan> findAllWithRoutinesAndExerciseSeriesByClinicId(ClinicId clinicId);

    List<TreatmentPlan> findAllByClinicIdAndPatientId(ClinicId clinicId, PatientId patientId);

    boolean existsByPatientId(PatientId patientId);

    @EntityGraph(attributePaths = {"routines"})
    @Query("select t from TreatmentPlan t where t.clinicId = :clinicId and t.patientId = :patientId order by t.period.startsAt desc")
    List<TreatmentPlan> findAllWithRoutinesAndExerciseSeriesByClinicIdAndPatientIdOrderByPeriodStartsAtDesc(
            ClinicId clinicId,
            PatientId patientId
    );

    @EntityGraph(attributePaths = {"routines"})
    List<TreatmentPlan> findAllWithRoutinesAndExerciseSeriesByClinicIdAndPatientIdAndStatusOrderByPeriodStartsAtAsc(
            ClinicId clinicId,
            PatientId patientId,
            TreatmentPlanStatus status
    );

    @EntityGraph(attributePaths = {"routines"})
    Optional<TreatmentPlan> findFirstWithRoutinesAndExerciseSeriesByClinicIdAndPatientIdAndStatusOrderByPeriodStartsAtAsc(
            ClinicId clinicId,
            PatientId patientId,
            TreatmentPlanStatus status
    );

    boolean existsByClinicIdAndPlanName(ClinicId clinicId, PlanName planName);
}
