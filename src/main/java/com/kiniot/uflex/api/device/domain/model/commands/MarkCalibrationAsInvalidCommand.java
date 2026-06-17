package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceId;

public record MarkCalibrationAsInvalidCommand(
        DeviceId deviceId
) {
    public MarkCalibrationAsInvalidCommand {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID cannot be null");
        }
    }
}
