package com.kiniot.uflex.api.planning.domain.model.queries;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record GetTreatmentPlanByPatientIdAndTreatmentPlanIdQuery(
        PatientId patientId,
        TreatmentPlanId treatmentPlanId
) {
    public GetTreatmentPlanByPatientIdAndTreatmentPlanIdQuery {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
    }
}
