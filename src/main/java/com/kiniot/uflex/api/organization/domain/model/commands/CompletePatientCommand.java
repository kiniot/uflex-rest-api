package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record CompletePatientCommand(
        PatientId patientId
) {
    public CompletePatientCommand {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }
}
