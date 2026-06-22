package com.kiniot.uflex.api.device.interfaces.rest.transform;

import com.kiniot.uflex.api.device.domain.model.queries.GlobalDeviceOverview;
import com.kiniot.uflex.api.device.interfaces.rest.resources.ClinicDeviceCountResource;
import com.kiniot.uflex.api.device.interfaces.rest.resources.GlobalDeviceOverviewResource;

public class GlobalDeviceOverviewResourceFromResultAssembler {

    private GlobalDeviceOverviewResourceFromResultAssembler() {}

    public static GlobalDeviceOverviewResource toResourceFromResult(GlobalDeviceOverview overview) {
        var perClinic = overview.perClinic().stream()
                .map(c -> new ClinicDeviceCountResource(c.clinicId(), c.clinicName(), c.count()))
                .toList();
        return new GlobalDeviceOverviewResource(
                overview.total(),
                overview.inStock(),
                overview.available(),
                overview.assigned(),
                overview.inMaintenance(),
                overview.retired(),
                overview.distinctClinics(),
                perClinic
        );
    }
}
