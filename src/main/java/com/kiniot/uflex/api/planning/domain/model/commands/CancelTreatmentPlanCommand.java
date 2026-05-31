package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;

public record CancelTreatmentPlanCommand(TreatmentPlanId treatmentPlanId) {
    public CancelTreatmentPlanCommand {
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
    }
}
