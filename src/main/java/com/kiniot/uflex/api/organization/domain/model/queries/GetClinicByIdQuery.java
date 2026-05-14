package com.kiniot.uflex.api.organization.domain.model.queries;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

public record GetClinicByIdQuery(
        ClinicId clinicId
) {
    public GetClinicByIdQuery {
        if (clinicId == null) {
            throw new IllegalArgumentException("Clinic ID cannot be null");
        }
    }
}
