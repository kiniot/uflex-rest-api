package com.kiniot.uflex.api.device.domain.model.queries;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record GetMyAssignedDeviceQuery(
        PatientId patientId
) {
    public GetMyAssignedDeviceQuery {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }
}