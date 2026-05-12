package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.entities.Routine;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.ExerciseSeriesResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.RoutineResource;

import java.util.List;

public class RoutineResourceFromEntityAssembler {
    public static RoutineResource toResourceFromEntity(Routine entity) {
        var routineId = entity.getId() != null ? entity.getId().id().toString() : null;
        var routineName = entity.getName() != null ? entity.getName().name() : null;
        var routineOrder = entity.getOrder() != null ? entity.getOrder().value() : null;
        var schedule = entity.getSchedule() != null
                ? RoutineScheduleResourceFromValueObjectAssembler.toResourceFromValueObject(entity.getSchedule())
                : null;
        List<ExerciseSeriesResource> exerciseSeries = entity.getExerciseSeries() != null
                ? entity.getExerciseSeries().stream().map(ExerciseSeriesResourceFromValueObjectAssembler::toResourceFromValueObject).toList()
                : List.of();

        return new RoutineResource(
                routineId,
                routineName,
                routineOrder,
                schedule,
                exerciseSeries);
    }
}
