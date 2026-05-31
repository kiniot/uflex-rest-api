package com.kiniot.uflex.api.planning.application.internal.queryservices;

import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.entities.Routine;
import com.kiniot.uflex.api.planning.domain.model.queries.GetActiveTreatmentPlanByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetAllTreatmentPlansQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetNextScheduledTreatmentPlanByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetScheduledTreatmentPlansByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlanByIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlanByPatientIdAndTreatmentPlanIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlansByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanStatus;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanQueryService;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.RoutineRepository;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.TreatmentPlanRepository;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.specifications.TreatmentPlanSpecificationBuilder;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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
        var specification = buildSpecification(query, clinicId);
        return treatmentPlanRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "period.startsAt")).stream()
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

    @Override
    @Transactional(readOnly = true)
    public Optional<TreatmentPlan> handle(GetActiveTreatmentPlanByPatientIdQuery query) {
        var clinicId = fetchAndValidatePatientClinic(query.patientId());
        return treatmentPlanRepository.findFirstWithRoutinesAndExerciseSeriesByClinicIdAndPatientIdAndStatusOrderByPeriodStartsAtAsc(
                        clinicId,
                        query.patientId(),
                        TreatmentPlanStatus.ACTIVE
                )
                .map(this::initializePlanGraph);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentPlan> handle(GetScheduledTreatmentPlansByPatientIdQuery query) {
        var clinicId = fetchAndValidatePatientClinic(query.patientId());
        return treatmentPlanRepository.findAllWithRoutinesAndExerciseSeriesByClinicIdAndPatientIdAndStatusOrderByPeriodStartsAtAsc(
                        clinicId,
                        query.patientId(),
                        TreatmentPlanStatus.SCHEDULED
                ).stream()
                .map(this::initializePlanGraph)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TreatmentPlan> handle(GetTreatmentPlanByPatientIdAndTreatmentPlanIdQuery query) {
        var clinicId = fetchAndValidatePatientClinic(query.patientId());
        return treatmentPlanRepository.findWithRoutinesAndExerciseSeriesByIdAndClinicIdAndPatientId(
                        query.treatmentPlanId(),
                        clinicId,
                        query.patientId()
                )
                .map(this::initializePlanGraph);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TreatmentPlan> handle(GetNextScheduledTreatmentPlanByPatientIdQuery query) {
        var clinicId = fetchAndValidatePatientClinic(query.patientId());
        return treatmentPlanRepository.findFirstWithRoutinesAndExerciseSeriesByClinicIdAndPatientIdAndStatusOrderByPeriodStartsAtAsc(
                        clinicId,
                        query.patientId(),
                        TreatmentPlanStatus.SCHEDULED
                )
                .map(this::initializePlanGraph);
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

    private Specification<TreatmentPlan> buildSpecification(GetAllTreatmentPlansQuery query, ClinicId clinicId) {
        if (query.patientId() != null)
            externalOrganizationService.validatePatientBelongsToClinic(query.patientId(), clinicId);
        var specificationBuilder = TreatmentPlanSpecificationBuilder.forClinic(clinicId)
                .withQuery(query);
        if (query.physiotherapistId() != null) {
            var patientIds = externalOrganizationService.findPatientIdsByPhysiotherapistAndClinic(query.physiotherapistId(), clinicId);
            specificationBuilder.withPatientIds(patientIds);
        }
        return specificationBuilder.build();
    }

    private ClinicId fetchAndValidatePatientClinic(com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId patientId) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        externalOrganizationService.validatePatientBelongsToClinic(patientId, clinicId);
        return clinicId;
    }
}
