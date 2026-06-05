package com.kiniot.uflex.api.device.domain.model.queries;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

public record GetAllDevicesByClinicIdQuery(
        ClinicId clinicId,
        DeviceStatus status
) {
    public GetAllDevicesByClinicIdQuery {
        if (clinicId == null) {
            throw new IllegalArgumentException("Clinic ID cannot be null");
        }
    }
}