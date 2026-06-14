package com.kiniot.uflex.api.device.domain.exceptions;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String serialNumber) {
        super("Device not found with serial number: %s".formatted(serialNumber));
    }
}
