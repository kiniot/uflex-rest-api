package com.kiniot.uflex.api.device.domain.exceptions;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;

public class DeviceNotInStockException extends RuntimeException {
    public DeviceNotInStockException(String serialNumber, DeviceStatus currentStatus) {
        super("Device " + serialNumber + " cannot be assigned to a clinic because it is not in stock. Current status: " + currentStatus);
    }
}
