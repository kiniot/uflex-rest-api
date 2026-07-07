package com.kiniot.uflex.api.device.interfaces.rest.resources;

public record DeviceHealthRowResource(
        String id,
        String serialNumber,
        String clinicId,
        String clinicName,
        String status,
        Integer batteryLevel,
        String lastSeenAt
) {
}
