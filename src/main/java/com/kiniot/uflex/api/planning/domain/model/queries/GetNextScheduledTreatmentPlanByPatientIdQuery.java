package com.kiniot.uflex.api.planning.domain.model.queries;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record GetNextScheduledTreatmentPlanByPatientIdQuery(PatientId patientId) {
    public GetNextScheduledTreatmentPlanByPatientIdQuery {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }
}
