package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateTreatmentPlanResource;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

import java.util.UUID;

public class CreateTreatmentPlanCommandFromResourceAssembler {
    public static CreateTreatmentPlanCommand toCommandFromResource(String patientId, CreateTreatmentPlanResource resource) {
        return new CreateTreatmentPlanCommand(
                new PatientId(UUID.fromString(patientId)),
                new PlanName(resource.name()),
                TreatmentPlanPeriodFromResourceAssembler.toValueObjectFromResource(resource.period()),
                resource.routines().stream()
                        .map(CreateTreatmentPlanRoutineCommandFromResourceAssembler::toCommandFromResource)
                        .toList());
    }
}
