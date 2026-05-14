package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.commands.AssignTreatmentPlanToPatientCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.AssignTreatmentPlanResource;

import java.util.UUID;

public class AssignTreatmentPlanCommandFromResourceAssembler {

    public static AssignTreatmentPlanToPatientCommand toCommandFromResource(
            String patientId,
            AssignTreatmentPlanResource resource) {
        return new AssignTreatmentPlanToPatientCommand(
                new PatientId(UUID.fromString(patientId)),
                new TreatmentPlanId(UUID.fromString(resource.treatmentPlanId()))
        );
    }
}