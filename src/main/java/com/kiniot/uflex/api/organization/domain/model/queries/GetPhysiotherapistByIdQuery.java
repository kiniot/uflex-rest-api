package com.kiniot.uflex.api.organization.domain.model.queries;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistId;

public record GetPhysiotherapistByIdQuery(
        PhysiotherapistId physiotherapistId
) {
    public GetPhysiotherapistByIdQuery {
        if (physiotherapistId == null) {
            throw new IllegalArgumentException("Physiotherapist ID cannot be null");
        }
    }
}