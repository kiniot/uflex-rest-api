package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.TreatmentPlanId;

public record AssignTreatmentPlanToPatientCommand(
        PatientId patientId,
        TreatmentPlanId treatmentPlanId
) {
    public AssignTreatmentPlanToPatientCommand {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
    }
}