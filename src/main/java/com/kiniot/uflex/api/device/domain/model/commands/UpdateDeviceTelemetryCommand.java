package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceId;

public record UpdateDeviceTelemetryCommand(
        DeviceId deviceId,
        Integer batteryLevel
) {
    public UpdateDeviceTelemetryCommand {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID cannot be null");
        }
        if (batteryLevel == null || batteryLevel < 0 || batteryLevel > 100) {
            throw new IllegalArgumentException("Battery level must be between 0 and 100");
        }
    }
}
