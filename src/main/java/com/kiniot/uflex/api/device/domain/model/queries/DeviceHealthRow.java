package com.kiniot.uflex.api.device.domain.model.queries;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;

import java.time.LocalDateTime;

/** A single affected device in the fleet-health view, with its owning clinic. */
public record DeviceHealthRow(
        String id,
        String serialNumber,
        String clinicId,
        String clinicName,
        DeviceStatus status,
        Integer batteryLevel,
        LocalDateTime lastSeenAt
) {
}
