package com.kiniot.uflex.api.device.interfaces.rest.resources;

import java.util.List;

public record FleetHealthResource(
        int offlineCount,
        int lowBatteryCount,
        int needsCalibrationCount,
        List<DeviceHealthRowResource> offline,
        List<DeviceHealthRowResource> lowBattery,
        List<DeviceHealthRowResource> needsCalibration
) {
}
