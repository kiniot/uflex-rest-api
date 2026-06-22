package com.kiniot.uflex.api.device.interfaces.rest.transform;

import com.kiniot.uflex.api.device.domain.model.queries.DeviceHealthRow;
import com.kiniot.uflex.api.device.domain.model.queries.FleetHealth;
import com.kiniot.uflex.api.device.interfaces.rest.resources.DeviceHealthRowResource;
import com.kiniot.uflex.api.device.interfaces.rest.resources.FleetHealthResource;

import java.util.List;

public class FleetHealthResourceFromResultAssembler {

    private FleetHealthResourceFromResultAssembler() {}

    public static FleetHealthResource toResourceFromResult(FleetHealth health) {
        return new FleetHealthResource(
                health.offlineCount(),
                health.lowBatteryCount(),
                health.needsCalibrationCount(),
                toRows(health.offline()),
                toRows(health.lowBattery()),
                toRows(health.needsCalibration()));
    }

    private static List<DeviceHealthRowResource> toRows(List<DeviceHealthRow> rows) {
        return rows.stream()
                .map(r -> new DeviceHealthRowResource(
                        r.id(),
                        r.serialNumber(),
                        r.clinicId(),
                        r.clinicName(),
                        r.status() != null ? r.status().name() : null,
                        r.batteryLevel(),
                        r.lastSeenAt() != null ? r.lastSeenAt().toString() : null))
                .toList();
    }
}
