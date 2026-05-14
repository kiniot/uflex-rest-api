package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanFrequency;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;

public record UpdateTreatmentPlanCommand(
        TreatmentPlanId treatmentPlanId,
        PlanName name,
        PlanFrequency frequency
) {
    public UpdateTreatmentPlanCommand {
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Plan name cannot be null");
        }
        if (frequency == null) {
            throw new IllegalArgumentException("Plan frequency cannot be null");
        }
    }
}
