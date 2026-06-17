package com.kiniot.uflex.api.device.interfaces.rest.resources;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;

public record UpdateDeviceStatusResource(
        DeviceStatus status
) {
}