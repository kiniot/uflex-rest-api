package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record AssignPatientToPhysiotherapistCommand(
        PatientId patientId,
        PhysiotherapistId physiotherapistId
) {
    public AssignPatientToPhysiotherapistCommand {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        if (physiotherapistId == null) {
            throw new IllegalArgumentException("Physiotherapist ID cannot be null");
        }
    }
}
