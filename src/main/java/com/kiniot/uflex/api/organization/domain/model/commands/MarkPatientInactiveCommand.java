package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record MarkPatientInactiveCommand(
        PatientId patientId
) {
    public MarkPatientInactiveCommand {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }
}
