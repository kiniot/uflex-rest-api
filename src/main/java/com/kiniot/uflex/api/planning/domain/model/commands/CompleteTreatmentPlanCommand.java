package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;

public record CompleteTreatmentPlanCommand(TreatmentPlanId treatmentPlanId) {
    public CompleteTreatmentPlanCommand {
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
    }
}
