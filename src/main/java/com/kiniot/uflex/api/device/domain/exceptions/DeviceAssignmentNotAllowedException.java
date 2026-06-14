package com.kiniot.uflex.api.device.domain.exceptions;

public class DeviceAssignmentNotAllowedException extends RuntimeException {
    public DeviceAssignmentNotAllowedException(String message) {
        super(message);
    }
}
