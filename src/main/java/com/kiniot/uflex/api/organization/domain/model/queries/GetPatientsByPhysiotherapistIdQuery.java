package com.kiniot.uflex.api.organization.domain.model.queries;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistId;

public record GetPatientsByPhysiotherapistIdQuery(
        PhysiotherapistId physiotherapistId
) {
    public GetPatientsByPhysiotherapistIdQuery {
        if (physiotherapistId == null) {
            throw new IllegalArgumentException("Physiotherapist ID cannot be null");
        }
    }
}