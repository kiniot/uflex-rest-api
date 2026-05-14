package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;

public record RemoveRoutineCommand(
        TreatmentPlanId treatmentPlanId,
        RoutineOrder order
) {
    public RemoveRoutineCommand {
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
        if (order == null) {
            throw new IllegalArgumentException("Routine order cannot be null");
        }
    }
}
