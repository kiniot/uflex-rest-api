package com.kiniot.uflex.api.device.interfaces.rest.resources;

import com.kiniot.uflex.api.device.domain.model.valueobjects.CalibrationStatus;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;

import java.time.LocalDateTime;

public record DeviceResource(
        String id,
        String serialNumber,
        String macAddress,
        String firmwareVersion,
        Integer batteryLevel,
        String model,
        CalibrationStatus calibrationStatus,
        DeviceStatus status,
        LocalDateTime lastSyncAt,
        String clinicId,
        String currentPatientId,
        String currentPatientFullName,
        boolean offline
) {
}