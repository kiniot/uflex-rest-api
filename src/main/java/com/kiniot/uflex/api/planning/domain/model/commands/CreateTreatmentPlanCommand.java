package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanFrequency;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;

public record CreateTreatmentPlanCommand(
        TreatmentPlanId id,
        PlanName name,
        PlanFrequency frequency
) {
    public CreateTreatmentPlanCommand {
        if (id == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
        if (name == null || name.name().isBlank()) {
            throw new IllegalArgumentException("Plan name cannot be null or blank");
        }
        if (frequency == null) {
            throw new IllegalArgumentException("Plan frequency cannot be null");
        }
    }
}
