package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDescription;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.MovementType;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateExerciseResource;

public class CreateExerciseCommandFromResourceAssembler {
    public static CreateExerciseCommand toCommandFromResource(CreateExerciseResource resource) {
        return new CreateExerciseCommand(
                new ExerciseName(resource.name()),
                new ExerciseDescription(resource.description()),
                BodyPart.valueOf(resource.bodyPart()),
                MovementType.valueOf(resource.movementType()),
                resource.videoAssetId() != null && !resource.videoAssetId().isBlank()
                        ? java.util.UUID.fromString(resource.videoAssetId())
                        : null);
    }
}
