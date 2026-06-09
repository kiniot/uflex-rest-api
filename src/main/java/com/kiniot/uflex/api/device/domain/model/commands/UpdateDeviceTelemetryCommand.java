package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;

public record UpdateDeviceTelemetryCommand(
        SerialNumber serialNumber,
        Integer batteryLevel
) {
    public UpdateDeviceTelemetryCommand {
        if (serialNumber == null) {
            throw new IllegalArgumentException("Serial number cannot be null");
        }
        if (batteryLevel == null || batteryLevel < 0 || batteryLevel > 100) {
            throw new IllegalArgumentException("Battery level must be between 0 and 100");
        }
    }
}