package com.kiniot.uflex.api.device.domain.exceptions;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String identifier) {
        super("Device not found with identifier: %s".formatted(identifier));
    }
}
