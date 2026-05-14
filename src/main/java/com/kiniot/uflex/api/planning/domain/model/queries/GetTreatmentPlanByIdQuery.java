package com.kiniot.uflex.api.planning.domain.model.queries;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;

public record GetTreatmentPlanByIdQuery(TreatmentPlanId treatmentPlanId) {
    public GetTreatmentPlanByIdQuery {
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
    }
}
