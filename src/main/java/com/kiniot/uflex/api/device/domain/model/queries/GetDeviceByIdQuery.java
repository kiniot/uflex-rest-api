package com.kiniot.uflex.api.device.domain.model.queries;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceId;

public record GetDeviceByIdQuery(
        DeviceId deviceId
) {
    public GetDeviceByIdQuery {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID cannot be null");
        }
    }
}
