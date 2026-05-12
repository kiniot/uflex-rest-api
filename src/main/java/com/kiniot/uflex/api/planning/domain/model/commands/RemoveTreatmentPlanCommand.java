package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;

public record RemoveTreatmentPlanCommand(
        TreatmentPlanId treatmentPlanId
) {
    public RemoveTreatmentPlanCommand {
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
    }
}
