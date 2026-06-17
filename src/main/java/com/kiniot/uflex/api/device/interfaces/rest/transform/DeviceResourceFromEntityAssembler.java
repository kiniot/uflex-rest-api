package com.kiniot.uflex.api.device.interfaces.rest.transform;

import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.interfaces.rest.resources.DeviceResource;

public class DeviceResourceFromEntityAssembler {

    private DeviceResourceFromEntityAssembler() {}

    public static DeviceResource toResourceFromEntity(Device entity, String patientFullName) {
        return new DeviceResource(
                entity.getId().id().toString(),
                entity.getSerialNumber().value(),
                entity.getMacAddress().value(),
                entity.getFirmwareVersion() != null ? entity.getFirmwareVersion().value() : null,
                entity.getBatteryLevel() != null ? entity.getBatteryLevel().percentage() : null,
                entity.getModel() != null ? entity.getModel().modelName() : null,
                entity.getAdvertisedName() != null ? entity.getAdvertisedName().value() : null,
                entity.getCalibrationStatus(),
                entity.getStatus(),
                entity.getLastSeenAt(),
                entity.getClinicId().id().toString(),
                entity.getCurrentPatientId() != null ? entity.getCurrentPatientId().patientId().toString() : null,
                patientFullName,
                entity.isOffline()
        );
    }
}
