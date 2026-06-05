package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;
import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;

public record UpdateDeviceStatusCommand(
        SerialNumber serialNumber,
        DeviceStatus status
) {
    public UpdateDeviceStatusCommand {
        if (serialNumber == null) {
            throw new IllegalArgumentException("Serial number cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }
}