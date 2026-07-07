package com.kiniot.uflex.api.device.domain.model.queries;

/**
 * Number of devices currently owned by a clinic, with its display name. Part of the
 * global device overview.
 */
public record ClinicDeviceCount(
        String clinicId,
        String clinicName,
        int count
) {
}
