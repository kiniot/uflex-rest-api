package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record AssignDeviceToPatientCommand(
        DeviceId deviceId,
        PatientId patientId
) {
    public AssignDeviceToPatientCommand {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID cannot be null");
        }
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }
}
