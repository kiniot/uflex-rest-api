package com.kiniot.uflex.api.device.domain.services;

import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.commands.*;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

public interface DeviceCommandService {
    Device handle(RegisterDeviceCommand command, ClinicId clinicId);
    void handle(AssignDeviceToPatientCommand command);
    void handle(ReturnDeviceCommand command);
    void handle(UpdateDeviceTelemetryCommand command);
    void handle(RegisterSuccessfulCalibrationCommand command);
    void handle(MarkCalibrationAsInvalidCommand command);
    void handle(UpdateDeviceStatusCommand command);
    void handle(DeleteDeviceCommand command);
}