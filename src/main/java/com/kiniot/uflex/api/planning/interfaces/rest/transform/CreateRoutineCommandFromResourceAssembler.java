package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateRoutineResource;

import java.util.UUID;

public class CreateRoutineCommandFromResourceAssembler {
    public static CreateRoutineCommand toCommandFromResource(String treatmentPlanId, CreateRoutineResource resource) {
        return new CreateRoutineCommand(
                new TreatmentPlanId(UUID.fromString(treatmentPlanId)),
                new RoutineName(resource.name()),
                new RoutineOrder(resource.order()),
                RoutineScheduleFromResourceAssembler.toValueObjectFromResource(resource.schedule()),
                ExerciseSeriesFromResourceAssembler.toValueObjectListFromResource(resource.exerciseSeries()));
    }
}
