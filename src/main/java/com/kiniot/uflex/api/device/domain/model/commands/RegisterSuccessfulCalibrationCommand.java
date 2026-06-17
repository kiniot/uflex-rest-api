package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceId;

public record RegisterSuccessfulCalibrationCommand(
        DeviceId deviceId
) {
    public RegisterSuccessfulCalibrationCommand {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID cannot be null");
        }
    }
}
