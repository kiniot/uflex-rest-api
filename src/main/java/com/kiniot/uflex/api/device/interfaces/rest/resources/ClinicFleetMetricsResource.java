package com.kiniot.uflex.api.device.interfaces.rest.resources;

public record ClinicFleetMetricsResource(
        int total,
        int available,
        int assigned,
        int inMaintenance,
        int lowBattery,
        int offline
) {
}