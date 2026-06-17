package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateTreatmentPlanRoutineResource;

public class CreateTreatmentPlanRoutineCommandFromResourceAssembler {
    public static CreateTreatmentPlanRoutineCommand toCommandFromResource(CreateTreatmentPlanRoutineResource resource) {
        return new CreateTreatmentPlanRoutineCommand(
                new RoutineName(resource.name()),
                new RoutineOrder(resource.order()),
                RoutineScheduleFromResourceAssembler.toValueObjectFromResource(resource.schedule()),
                ExerciseSeriesFromResourceAssembler.toValueObjectListFromResource(resource.exerciseSeries()));
    }
}
