package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.entities.Routine;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.RoutineResource;

public class RoutineResourceFromEntityAssembler {
    public static RoutineResource toResourceFromEntity(Routine entity) {
        var routineId = entity.getId() != null ? entity.getId().id().toString() : null;
        var exerciseSeries = entity.getExerciseSeries() != null
                ? entity.getExerciseSeries().stream().map(ExerciseSeriesResourceFromValueObjectAssembler::toResourceFromValueObject).toList()
                : java.util.List.of();

        return new RoutineResource(
                routineId,
                entity.getName().name(),
                entity.getOrder().value(),
                RoutineScheduleResourceFromValueObjectAssembler.toResourceFromValueObject(entity.getSchedule()),
                exerciseSeries);
    }
}
