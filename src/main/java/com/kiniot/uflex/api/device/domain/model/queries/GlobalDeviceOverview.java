package com.kiniot.uflex.api.device.domain.model.queries;

import java.util.List;

/**
 * Aggregate, cross-clinic view of the device inventory for the developer console.
 */
public record GlobalDeviceOverview(
        int total,
        int inStock,
        int available,
        int assigned,
        int inMaintenance,
        int retired,
        int distinctClinics,
        List<ClinicDeviceCount> perClinic
) {
}
