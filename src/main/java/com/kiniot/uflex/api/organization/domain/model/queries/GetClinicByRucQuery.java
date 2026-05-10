package com.kiniot.uflex.api.organization.domain.model.queries;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.Ruc;

public record GetClinicByRucQuery(
        Ruc ruc
) {
    public GetClinicByRucQuery {
        if (ruc == null) {
            throw new IllegalArgumentException("RUC cannot be null");
        }
    }
}