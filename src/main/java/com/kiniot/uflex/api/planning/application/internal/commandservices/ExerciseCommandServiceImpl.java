package com.kiniot.uflex.api.planning.application.internal.commandservices;

import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseWithIdNotFoundException;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.entities.Exercise;
import com.kiniot.uflex.api.planning.domain.services.ExerciseCommandService;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ExerciseCommandServiceImpl implements ExerciseCommandService {

    private final ExerciseRepository exerciseRepository;

    public ExerciseCommandServiceImpl(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    @Transactional
    public Optional<Exercise> handle(CreateExerciseCommand command) {
        var exercise = new Exercise(command);
        return Optional.of(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public Optional<Exercise> handle(UpdateExerciseCommand command) {
        var exercise = exerciseRepository.findById(command.exerciseId())
                .orElseThrow(() -> new ExerciseWithIdNotFoundException(command.exerciseId().id().toString()));
        exercise.update(command);
        return Optional.of(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public void handle(RemoveExerciseCommand command) {
        var exercise = exerciseRepository.findById(command.exerciseId())
                .orElseThrow(() -> new ExerciseWithIdNotFoundException(command.exerciseId().id().toString()));
        exerciseRepository.delete(exercise);
    }
}
