package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;

public record RemoveExerciseCommand(
        ExerciseId exerciseId
) {
    public RemoveExerciseCommand {
        if (exerciseId == null) {
            throw new IllegalArgumentException("Exercise ID cannot be null");
        }
    }
}
