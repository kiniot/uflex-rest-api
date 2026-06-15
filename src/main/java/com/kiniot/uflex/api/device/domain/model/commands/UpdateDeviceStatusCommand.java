package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceId;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;

public record UpdateDeviceStatusCommand(
        DeviceId deviceId,
        DeviceStatus status
) {
    public UpdateDeviceStatusCommand {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }
}
