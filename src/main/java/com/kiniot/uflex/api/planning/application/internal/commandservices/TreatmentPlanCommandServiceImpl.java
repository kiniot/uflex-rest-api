package com.kiniot.uflex.api.planning.application.internal.commandservices;

import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseClinicMismatchException;
import com.kiniot.uflex.api.planning.domain.exceptions.TreatmentPlanWithIdNotFoundException;
import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanCommandService;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.ExerciseRepository;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.TreatmentPlanRepository;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TreatmentPlanCommandServiceImpl implements TreatmentPlanCommandService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExternalIamService externalIamService;

    public TreatmentPlanCommandServiceImpl(
            TreatmentPlanRepository treatmentPlanRepository,
            ExerciseRepository exerciseRepository,
            ExternalIamService externalIamService
    ) {
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.exerciseRepository = exerciseRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(CreateTreatmentPlanCommand command) {
        var clinicId = externalIamService.fetchCurrentAcademyId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var treatmentPlan = new TreatmentPlan(command, clinicId);
        return Optional.of(treatmentPlanRepository.save(treatmentPlan));
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(UpdateTreatmentPlanCommand command) {
        var treatmentPlan = getTreatmentPlanOrThrow(command.treatmentPlanId());
        treatmentPlan.update(command);
        return Optional.of(treatmentPlanRepository.save(treatmentPlan));
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
        return Optional.of(treatmentPlanRepository.save(treatmentPlan));
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(UpdateRoutineCommand command) {
        var treatmentPlan = getTreatmentPlanOrThrow(command.treatmentPlanId());
        validateExercisesBelongToClinic(command.exerciseSeries().stream().map(series -> series.exerciseId()).toList(), treatmentPlan.getClinicId());
        treatmentPlan.updateRoutine(command);
        return Optional.of(treatmentPlanRepository.save(treatmentPlan));
    }

    @Override
    @Transactional
    public Optional<TreatmentPlan> handle(RemoveRoutineCommand command) {
        var treatmentPlan = getTreatmentPlanOrThrow(command.treatmentPlanId());
        treatmentPlan.removeRoutine(command);
        return Optional.of(treatmentPlanRepository.save(treatmentPlan));
    }

    private TreatmentPlan getTreatmentPlanOrThrow(TreatmentPlanId treatmentPlanId) {
        var clinicId = externalIamService.fetchCurrentAcademyId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return treatmentPlanRepository.findWithRoutinesAndExerciseSeriesByIdAndClinicId(treatmentPlanId, clinicId)
                .orElseThrow(() -> new TreatmentPlanWithIdNotFoundException(treatmentPlanId.id().toString()));
    }

    private void validateExercisesBelongToClinic(List<ExerciseId> exerciseIds, ClinicId clinicId) {
        for (ExerciseId exerciseId : exerciseIds) {
            boolean belongsToClinic = exerciseRepository.findByIdAndClinicId(exerciseId, clinicId).isPresent();
            if (!belongsToClinic) {
                throw new ExerciseClinicMismatchException(exerciseId.id().toString(), clinicId.clinicId().toString());
            }
        }
    }
}
