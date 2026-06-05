package com.kiniot.uflex.api.device.domain.model.queries;

public record ClinicFleetMetrics(
        int total,
        int available,
        int assigned,
        int inMaintenance,
        int lowBattery,
        int offline
) {}