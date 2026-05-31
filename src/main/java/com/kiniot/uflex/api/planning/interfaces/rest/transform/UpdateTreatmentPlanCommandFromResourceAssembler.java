package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.commands.UpdateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.UpdateTreatmentPlanResource;

import java.util.UUID;

public class UpdateTreatmentPlanCommandFromResourceAssembler {
    public static UpdateTreatmentPlanCommand toCommandFromResource(String treatmentPlanId, UpdateTreatmentPlanResource resource) {
        return new UpdateTreatmentPlanCommand(
                new TreatmentPlanId(UUID.fromString(treatmentPlanId)),
                new PlanName(resource.name()),
                TreatmentPlanPeriodFromResourceAssembler.toValueObjectFromResource(resource.period()));
    }
}
