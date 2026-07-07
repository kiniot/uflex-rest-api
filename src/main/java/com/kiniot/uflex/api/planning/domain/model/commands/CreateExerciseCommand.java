package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDescription;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.MovementType;

import java.util.UUID;

public record CreateExerciseCommand(
        ExerciseName name,
        ExerciseDescription description,
        BodyPart bodyPart,
        MovementType movementType,
        UUID videoAssetId
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
        if (movementType == null) {
            throw new IllegalArgumentException("Movement type cannot be null");
        }
    }
}
