package com.kiniot.uflex.api.planning.application.internal.queryservices;

import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.entities.Routine;
import com.kiniot.uflex.api.planning.domain.model.queries.GetAllTreatmentPlansQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlanByIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlansByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanQueryService;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.RoutineRepository;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.TreatmentPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TreatmentPlanQueryServiceImpl implements TreatmentPlanQueryService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final RoutineRepository routineRepository;
    private final ExternalIamService externalIamService;
    private final ExternalOrganizationService externalOrganizationService;

    public TreatmentPlanQueryServiceImpl(
            TreatmentPlanRepository treatmentPlanRepository,
            RoutineRepository routineRepository,
            ExternalIamService externalIamService,
            ExternalOrganizationService externalOrganizationService
    ) {
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.routineRepository = routineRepository;
        this.externalIamService = externalIamService;
        this.externalOrganizationService = externalOrganizationService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TreatmentPlan> handle(GetTreatmentPlanByIdQuery query) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return treatmentPlanRepository.findWithRoutinesAndExerciseSeriesByIdAndClinicId(query.treatmentPlanId(), clinicId)
                .map(this::initializePlanGraph);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentPlan> handle(GetAllTreatmentPlansQuery query) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return treatmentPlanRepository.findAllWithRoutinesAndExerciseSeriesByClinicId(clinicId).stream()
                .map(this::initializePlanGraph)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentPlan> handle(GetTreatmentPlansByPatientIdQuery query) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        externalOrganizationService.validatePatientBelongsToClinic(query.patientId(), clinicId);
        return treatmentPlanRepository.findAllWithRoutinesAndExerciseSeriesByClinicIdAndPatientIdOrderByPeriodStartsAtDesc(
                clinicId,
                query.patientId()
        ).stream()
                .map(this::initializePlanGraph)
                .toList();
    }

    private TreatmentPlan initializePlanGraph(TreatmentPlan treatmentPlan) {
        loadExerciseSeriesForRoutines(List.of(treatmentPlan));
        return treatmentPlan;
    }

    private void loadExerciseSeriesForRoutines(List<TreatmentPlan> treatmentPlans) {
        var routineIds = treatmentPlans.stream()
                .flatMap(plan -> plan.getRoutines().stream())
                .map(Routine::getId)
                .toList();

        if (routineIds.isEmpty()) {
            return;
        }

        routineRepository.findAllByIdIn(routineIds);
    }
}
