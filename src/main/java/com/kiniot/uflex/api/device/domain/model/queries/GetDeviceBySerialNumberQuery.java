package com.kiniot.uflex.api.device.domain.model.queries;

import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;

public record GetDeviceBySerialNumberQuery(
        SerialNumber serialNumber
) {
    public GetDeviceBySerialNumberQuery {
        if (serialNumber == null) {
            throw new IllegalArgumentException("Serial number cannot be null");
        }
    }
}