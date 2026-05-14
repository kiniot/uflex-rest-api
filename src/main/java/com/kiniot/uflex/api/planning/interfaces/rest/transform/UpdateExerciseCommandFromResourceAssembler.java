package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.commands.UpdateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDescription;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseName;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.UpdateExerciseResource;

import java.util.UUID;

public class UpdateExerciseCommandFromResourceAssembler {
    public static UpdateExerciseCommand toCommandFromResource(String exerciseId, UpdateExerciseResource resource) {
        return new UpdateExerciseCommand(
                new ExerciseId(UUID.fromString(exerciseId)),
                new ExerciseName(resource.name()),
                new ExerciseDescription(resource.description()),
                new BodyPart(resource.bodyPart()));
    }
}
