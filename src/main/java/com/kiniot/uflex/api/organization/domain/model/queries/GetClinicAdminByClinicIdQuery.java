package com.kiniot.uflex.api.organization.domain.model.queries;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

public record GetClinicAdminByClinicIdQuery(
        ClinicId clinicId
) {
    public GetClinicAdminByClinicIdQuery {
        if (clinicId == null) {
            throw new IllegalArgumentException("Clinic ID cannot be null");
        }
    }
}
