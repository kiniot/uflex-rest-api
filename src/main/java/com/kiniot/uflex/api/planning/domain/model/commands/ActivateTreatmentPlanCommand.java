package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;

public record ActivateTreatmentPlanCommand(TreatmentPlanId treatmentPlanId) {
    public ActivateTreatmentPlanCommand {
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
    }
}
