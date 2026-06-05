package com.kiniot.uflex.api.device.domain.model.queries;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

public record GetClinicFleetMetricsQuery(
        ClinicId clinicId
) {
    public GetClinicFleetMetricsQuery {
        if (clinicId == null) {
            throw new IllegalArgumentException("Clinic ID cannot be null");
        }
    }
}