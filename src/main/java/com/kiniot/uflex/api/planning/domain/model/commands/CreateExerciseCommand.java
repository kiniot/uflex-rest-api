package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDescription;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseName;

public record CreateExerciseCommand(
        ExerciseName name,
        ExerciseDescription description,
        BodyPart bodyPart
) {
    public CreateExerciseCommand {
        if (name == null) {
            throw new IllegalArgumentException("Exercise name cannot be null");
        }
        if (description == null) {
            throw new IllegalArgumentException("Exercise description cannot be null");
        }
        if (bodyPart == null) {
            throw new IllegalArgumentException("Body part cannot be null");
        }
    }
}
