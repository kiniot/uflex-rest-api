package com.kiniot.uflex.api.device.interfaces.rest.transform;

import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.interfaces.rest.resources.DeviceResource;

public class DeviceResourceFromEntityAssembler {

    private DeviceResourceFromEntityAssembler() {}

    public static DeviceResource toResourceFromEntity(Device entity, ExternalOrganizationService externalOrganizationService) {
        String patientFullName = null;
        if (entity.getCurrentPatientId() != null && externalOrganizationService != null) {
            patientFullName = externalOrganizationService.getPatientFullName(
                    entity.getCurrentPatientId().patientId().toString()
            );
        }

        return new DeviceResource(
                entity.getId().id().toString(),
                entity.getSerialNumber().value(),
                entity.getMacAddress().value(),
                entity.getFirmwareVersion() != null ? entity.getFirmwareVersion().value() : null,
                entity.getBatteryLevel() != null ? entity.getBatteryLevel().percentage() : null,
                entity.getModel() != null ? entity.getModel().modelName() : null,
                entity.getCalibrationStatus(),
                entity.getStatus(),
                entity.getLastSyncAt(),
                entity.getClinicId().id().toString(),
                entity.getCurrentPatientId() != null ? entity.getCurrentPatientId().patientId().toString() : null,
                patientFullName,
                entity.isOffline()
        );
    }
}