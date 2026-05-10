package com.kiniot.uflex.api.organization.domain.model.queries;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.PatientId;

public record GetPatientByIdQuery(
        PatientId patientId
) {
    public GetPatientByIdQuery {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }
}