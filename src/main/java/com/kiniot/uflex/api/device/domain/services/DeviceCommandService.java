package com.kiniot.uflex.api.device.domain.services;

import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.commands.*;

public interface DeviceCommandService {
    Device handle(RegisterDeviceCommand command);
    void handle(AssignDeviceToPatientCommand command);
    void handle(ReturnDeviceCommand command);
    void handle(UpdateDeviceTelemetryCommand command);
    void handle(RegisterSuccessfulCalibrationCommand command);
    void handle(MarkCalibrationAsInvalidCommand command);
    void handle(UpdateDeviceStatusCommand command);
    void handle(DeleteDeviceCommand command);

    /**
     * Assigns stock devices to the clinic to cover the shortfall between the kits it paid
     * for and the kits it already owns, capped by available stock. Returns how many were
     * actually assigned (may be less than requested when stock is short).
     */
    int handle(AssignStockToClinicCommand command);

    /**
     * Seeds demo stock devices (local/demo only). Returns how many were newly created.
     */
    int handle(SeedDevicesCommand command);
}