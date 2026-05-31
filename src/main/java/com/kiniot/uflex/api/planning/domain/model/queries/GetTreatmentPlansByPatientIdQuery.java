package com.kiniot.uflex.api.planning.domain.model.queries;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record GetTreatmentPlansByPatientIdQuery(PatientId patientId) {
    public GetTreatmentPlansByPatientIdQuery {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }
}
