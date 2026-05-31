package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.commands.UpdateRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.UpdateRoutineResource;

import java.util.UUID;

public class UpdateRoutineCommandFromResourceAssembler {
    public static UpdateRoutineCommand toCommandFromResource(String treatmentPlanId, Integer currentOrder, UpdateRoutineResource resource) {
        return new UpdateRoutineCommand(
                new TreatmentPlanId(UUID.fromString(treatmentPlanId)),
                new RoutineOrder(currentOrder),
                new RoutineName(resource.name()),
                new RoutineOrder(resource.newOrder()),
                RoutineScheduleFromResourceAssembler.toValueObjectFromResource(resource.schedule()),
                ExerciseSeriesFromResourceAssembler.toValueObjectListFromResource(resource.exerciseSeries()));
    }
}
