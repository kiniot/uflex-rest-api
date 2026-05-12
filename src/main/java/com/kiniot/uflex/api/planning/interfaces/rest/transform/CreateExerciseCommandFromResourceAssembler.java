package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDescription;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseName;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateExerciseResource;

public class CreateExerciseCommandFromResourceAssembler {
    public static CreateExerciseCommand toCommandFromResource(CreateExerciseResource resource) {
        return new CreateExerciseCommand(
                new ExerciseName(resource.name()),
                new ExerciseDescription(resource.description()),
                new BodyPart(resource.bodyPart()));
    }
}
