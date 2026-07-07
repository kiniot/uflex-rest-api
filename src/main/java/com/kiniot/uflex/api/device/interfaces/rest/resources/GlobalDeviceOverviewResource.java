package com.kiniot.uflex.api.device.interfaces.rest.resources;

import java.util.List;

public record GlobalDeviceOverviewResource(
        int total,
        int inStock,
        int available,
        int assigned,
        int inMaintenance,
        int retired,
        int distinctClinics,
        List<ClinicDeviceCountResource> perClinic
) {
}
