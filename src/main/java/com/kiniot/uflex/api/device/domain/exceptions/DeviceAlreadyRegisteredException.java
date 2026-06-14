package com.kiniot.uflex.api.device.domain.exceptions;

public class DeviceAlreadyRegisteredException extends RuntimeException {
    public DeviceAlreadyRegisteredException(String serialNumber) {
        super("Device with serial number %s already exists".formatted(serialNumber));
    }
}
