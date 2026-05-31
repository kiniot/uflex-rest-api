package com.kiniot.uflex.api.planning.application.internal.commandservices;

import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseClinicMismatchException;
import com.kiniot.uflex.api.planning.domain.exceptions.OverlappingTreatmentPlanPeriodException;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientAlreadyHasActiveTreatmentPlanException;
import com.kiniot.uflex.api.planning.domain.exceptions.TreatmentPlanWithIdNotFoundException;
import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.commands.ActivateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CancelTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CompleteTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.entities.Routine;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanPeriod;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanStatus;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanCommandService;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.ExerciseRepository;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.RoutineRepository;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.TreatmentPlanRepository;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TreatmentPlanCommandServiceImpl implements TreatmentPlanCommandService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final RoutineRepository routineRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExternalIamService externalIamService;
    private final ExternalOrganizationService externalOrganizationService;

    public TreatmentPlanCommandServiceImpl(
            TreatmentPlanRepository treatmentPlanRepository,
            RoutineRepository routineRepository,
            ExerciseRepository exerciseRepository,
            ExternalIamService externalIamService,
            ExternalOrganizationService externalOrganizationService
    ) {
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.routineRepository = routineRepository;
        this.exerciseRepository = exerciseRepository;
        this.externalIamService = externalIamService;
        this.externalOrganizationService = externalOrganizationService;
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(CreateTreatmentPlanCommand command) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        externalOrganizationService.validatePatientBelongsToClinic(command.patientId(), clinicId);
        validateExercisesBelongToClinic(
                command.routines().stream()
                        .flatMap(routine -> routine.exerciseSeries().stream())
                        .map(series -> series.exerciseId())
                        .toList(),
                clinicId);
        validatePatientPlanCoexistence(clinicId, command.patientId(), command.status(), command.period(), null);
        var treatmentPlan = new TreatmentPlan(command, clinicId);
        return Optional.of(initializePlanGraph(treatmentPlanRepository.save(treatmentPlan)));
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(UpdateTreatmentPlanCommand command) {
        var treatmentPlan = getTreatmentPlanOrThrow(command.treatmentPlanId());
        treatmentPlan.update(command);
        return Optional.of(initializePlanGraph(treatmentPlanRepository.save(treatmentPlan)));
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(ActivateTreatmentPlanCommand command) {
        var treatmentPlan = getTreatmentPlanOrThrow(command.treatmentPlanId());
        treatmentPlan.activate();
        validatePatientPlanCoexistence(
                treatmentPlan.getClinicId(),
                treatmentPlan.getPatientId(),
                treatmentPlan.getStatus(),
                treatmentPlan.getPeriod(),
                treatmentPlan.getId()
        );
        return Optional.of(initializePlanGraph(treatmentPlanRepository.save(treatmentPlan)));
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(CompleteTreatmentPlanCommand command) {
        var treatmentPlan = getTreatmentPlanOrThrow(command.treatmentPlanId());
        treatmentPlan.complete();
        return Optional.of(initializePlanGraph(treatmentPlanRepository.save(treatmentPlan)));
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(CancelTreatmentPlanCommand command) {
        var treatmentPlan = getTreatmentPlanOrThrow(command.treatmentPlanId());
        treatmentPlan.cancel();
        return Optional.of(initializePlanGraph(treatmentPlanRepository.save(treatmentPlan)));
    }

    @Override
    @Transactional
    public void handle(RemoveTreatmentPlanCommand command) {
        var treatmentPlan = getTreatmentPlanOrThrow(command.treatmentPlanId());
        treatmentPlanRepository.delete(treatmentPlan);
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(CreateRoutineCommand command) {
        var treatmentPlan = getTreatmentPlanOrThrow(command.treatmentPlanId());
        validateExercisesBelongToClinic(command.exerciseSeries().stream().map(series -> series.exerciseId()).toList(), treatmentPlan.getClinicId());
        treatmentPlan.addRoutine(command);
        return Optional.of(initializePlanGraph(treatmentPlanRepository.save(treatmentPlan)));
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(UpdateRoutineCommand command) {
        var treatmentPlan = getTreatmentPlanOrThrow(command.treatmentPlanId());
        validateExercisesBelongToClinic(command.exerciseSeries().stream().map(series -> series.exerciseId()).toList(), treatmentPlan.getClinicId());
        treatmentPlan.updateRoutine(command);
        return Optional.of(initializePlanGraph(treatmentPlanRepository.save(treatmentPlan)));
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(RemoveRoutineCommand command) {
        var treatmentPlan = getTreatmentPlanOrThrow(command.treatmentPlanId());
        treatmentPlan.removeRoutine(command);
        return Optional.of(initializePlanGraph(treatmentPlanRepository.save(treatmentPlan)));
    }

    private TreatmentPlan getTreatmentPlanOrThrow(TreatmentPlanId treatmentPlanId) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return treatmentPlanRepository.findWithRoutinesAndExerciseSeriesByIdAndClinicId(treatmentPlanId, clinicId)
                .orElseThrow(() -> new TreatmentPlanWithIdNotFoundException(treatmentPlanId.id().toString()));
    }

    private void validateExercisesBelongToClinic(List<ExerciseId> exerciseIds, ClinicId clinicId) {
        for (ExerciseId exerciseId : exerciseIds) {
            boolean belongsToClinic = exerciseRepository.findByIdAndClinicId(exerciseId, clinicId).isPresent();
            if (!belongsToClinic) {
                throw new ExerciseClinicMismatchException(exerciseId.id().toString(), clinicId.id().toString());
            }
        }
    }

    private void validatePatientPlanCoexistence(
            ClinicId clinicId,
            PatientId patientId,
            TreatmentPlanStatus candidateStatus,
            TreatmentPlanPeriod candidatePeriod,
            TreatmentPlanId planToIgnore
    ) {
        if (!isSchedulableStatus(candidateStatus)) {
            return;
        }

        var existingPlans = treatmentPlanRepository.findAllByClinicIdAndPatientId(clinicId, patientId).stream()
                .filter(plan -> planToIgnore == null || !plan.getId().equals(planToIgnore))
                .filter(plan -> isSchedulableStatus(plan.getStatus()))
                .toList();

        boolean hasAnotherActivePlan = candidateStatus == TreatmentPlanStatus.ACTIVE && existingPlans.stream()
                .anyMatch(plan -> plan.getStatus() == TreatmentPlanStatus.ACTIVE);
        if (hasAnotherActivePlan) {
            throw new PatientAlreadyHasActiveTreatmentPlanException(patientId.patientId().toString());
        }

        boolean hasOverlappingPlan = existingPlans.stream()
                .anyMatch(plan -> periodsOverlap(candidatePeriod, plan.getPeriod()));
        if (hasOverlappingPlan) {
            throw new OverlappingTreatmentPlanPeriodException(patientId.patientId().toString());
        }
    }

    private boolean isSchedulableStatus(TreatmentPlanStatus status) {
        return status == TreatmentPlanStatus.SCHEDULED || status == TreatmentPlanStatus.ACTIVE;
    }

    private boolean periodsOverlap(TreatmentPlanPeriod first, TreatmentPlanPeriod second) {
        LocalDate firstStart = first.startsAt();
        LocalDate firstEnd = first.endsAt();
        LocalDate secondStart = second.startsAt();
        LocalDate secondEnd = second.endsAt();

        return !firstEnd.isBefore(secondStart) && !secondEnd.isBefore(firstStart);
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
