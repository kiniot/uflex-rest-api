package com.kiniot.uflex.api.device.interfaces.rest.resources;

public record ClinicDeviceCountResource(
        String clinicId,
        String clinicName,
        int count
) {
}
