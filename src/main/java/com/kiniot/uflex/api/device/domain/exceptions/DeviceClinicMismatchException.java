package com.kiniot.uflex.api.device.domain.exceptions;

public class DeviceClinicMismatchException extends RuntimeException {
    public DeviceClinicMismatchException() {
        super("Device does not belong to your clinic");
    }
}
