package com.kiniot.uflex.api.planning.application.internal.commandservices;

import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseWithIdNotFoundException;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.aggregates.Exercise;
import com.kiniot.uflex.api.planning.domain.services.ExerciseCommandService;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ExerciseCommandServiceImpl implements ExerciseCommandService {

    private final ExerciseRepository exerciseRepository;
    private final ExternalIamService externalIamService;

    public ExerciseCommandServiceImpl(ExerciseRepository exerciseRepository, ExternalIamService externalIamService) {
        this.exerciseRepository = exerciseRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    @Transactional
    public Optional<Exercise> handle(CreateExerciseCommand command) {
        var clinicId = externalIamService.fetchCurrentAcademyId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var exercise = new Exercise(command, clinicId);
        return Optional.of(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public Optional<Exercise> handle(UpdateExerciseCommand command) {
        var clinicId = externalIamService.fetchCurrentAcademyId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var exercise = exerciseRepository.findByIdAndClinicId(command.exerciseId(), clinicId)
                .orElseThrow(() -> new ExerciseWithIdNotFoundException(command.exerciseId().id().toString()));
        exercise.update(command);
        return Optional.of(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public void handle(RemoveExerciseCommand command) {
        var clinicId = externalIamService.fetchCurrentAcademyId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var exercise = exerciseRepository.findByIdAndClinicId(command.exerciseId(), clinicId)
                .orElseThrow(() -> new ExerciseWithIdNotFoundException(command.exerciseId().id().toString()));
        exerciseRepository.delete(exercise);
    }
}
