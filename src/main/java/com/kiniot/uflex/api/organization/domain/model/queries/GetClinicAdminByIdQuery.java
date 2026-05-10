package com.kiniot.uflex.api.organization.domain.model.queries;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicAdminId;

public record GetClinicAdminByIdQuery(
        ClinicAdminId clinicAdminId
) {
    public GetClinicAdminByIdQuery {
        if (clinicAdminId == null) {
            throw new IllegalArgumentException("Clinic admin ID cannot be null");
        }
    }
}