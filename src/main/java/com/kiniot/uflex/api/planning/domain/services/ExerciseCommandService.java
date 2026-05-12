package com.kiniot.uflex.api.planning.domain.services;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.aggregates.Exercise;

import java.util.Optional;

public interface ExerciseCommandService {
    Optional<Exercise> handle(CreateExerciseCommand command);
    Optional<Exercise> handle(UpdateExerciseCommand command);
    void handle(RemoveExerciseCommand command);
}
