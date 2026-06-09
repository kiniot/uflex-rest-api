package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;

public record DeleteDeviceCommand(SerialNumber serialNumber) {
    public DeleteDeviceCommand {
        if (serialNumber == null) {
            throw new IllegalArgumentException("Serial number cannot be null");
        }
    }
}
