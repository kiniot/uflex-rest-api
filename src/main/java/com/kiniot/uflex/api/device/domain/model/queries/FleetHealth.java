package com.kiniot.uflex.api.device.domain.model.queries;

import java.util.List;

/** Cross-clinic device health: counts plus the affected devices per category. */
public record FleetHealth(
        int offlineCount,
        int lowBatteryCount,
        int needsCalibrationCount,
        List<DeviceHealthRow> offline,
        List<DeviceHealthRow> lowBattery,
        List<DeviceHealthRow> needsCalibration
) {
}
