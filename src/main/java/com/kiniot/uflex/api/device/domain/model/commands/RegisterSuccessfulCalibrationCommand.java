package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;

public record RegisterSuccessfulCalibrationCommand(
        SerialNumber serialNumber
) {
    public RegisterSuccessfulCalibrationCommand {
        if (serialNumber == null) {
            throw new IllegalArgumentException("Serial number cannot be null");
        }
    }
}