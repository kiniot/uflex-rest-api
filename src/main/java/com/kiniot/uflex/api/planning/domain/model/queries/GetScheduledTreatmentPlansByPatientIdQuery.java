package com.kiniot.uflex.api.planning.domain.model.queries;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record GetScheduledTreatmentPlansByPatientIdQuery(PatientId patientId) {
    public GetScheduledTreatmentPlansByPatientIdQuery {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }
}
