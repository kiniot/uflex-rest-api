package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceId;

public record ReturnDeviceCommand(
        DeviceId deviceId
) {
    public ReturnDeviceCommand {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID cannot be null");
        }
    }
}
