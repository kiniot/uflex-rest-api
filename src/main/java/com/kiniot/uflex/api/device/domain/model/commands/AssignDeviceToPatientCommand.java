package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record AssignDeviceToPatientCommand(
        SerialNumber serialNumber,
        PatientId patientId
) {
    public AssignDeviceToPatientCommand {
        if (serialNumber == null) {
            throw new IllegalArgumentException("Serial number cannot be null");
        }
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }
}