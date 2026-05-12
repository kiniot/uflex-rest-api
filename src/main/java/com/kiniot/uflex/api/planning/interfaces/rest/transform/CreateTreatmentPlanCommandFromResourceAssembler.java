package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateTreatmentPlanResource;

import java.util.UUID;

public class CreateTreatmentPlanCommandFromResourceAssembler {
    public static CreateTreatmentPlanCommand toCommandFromResource(CreateTreatmentPlanResource resource) {
        return new CreateTreatmentPlanCommand(
                new TreatmentPlanId(UUID.fromString(resource.id())),
                new PlanName(resource.name()),
                PlanFrequencyFromResourceAssembler.toValueObjectFromResource(resource.frequency()));
    }
}
