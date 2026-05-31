package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanPeriod;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanStatus;

public record UpdateTreatmentPlanCommand(
        TreatmentPlanId treatmentPlanId,
        PlanName name,
        TreatmentPlanStatus status,
        TreatmentPlanPeriod period
) {
    public UpdateTreatmentPlanCommand {
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Plan name cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Treatment plan status cannot be null");
        }
        if (period == null) {
            throw new IllegalArgumentException("Treatment plan period cannot be null");
        }
    }
}
