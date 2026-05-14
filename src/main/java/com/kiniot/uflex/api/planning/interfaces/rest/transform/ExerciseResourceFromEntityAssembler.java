package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.aggregates.Exercise;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.ExerciseResource;

public class ExerciseResourceFromEntityAssembler {
    public static ExerciseResource toResourceFromEntity(Exercise entity) {
        var exerciseId = entity.getId() != null ? entity.getId().id().toString() : null;
        var bodyPart = entity.getBodyPart() != null ? entity.getBodyPart().name() : null;
        return new ExerciseResource(
                exerciseId,
                entity.getName().name(),
                entity.getDescription().description(),
                bodyPart);
    }
}
