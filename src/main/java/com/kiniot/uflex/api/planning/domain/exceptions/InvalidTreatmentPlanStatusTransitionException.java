package com.kiniot.uflex.api.planning.domain.exceptions;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanStatus;

public class InvalidTreatmentPlanStatusTransitionException extends RuntimeException {
    public InvalidTreatmentPlanStatusTransitionException(TreatmentPlanStatus currentStatus, TreatmentPlanStatus targetStatus) {
        super("Cannot transition treatment plan status from %s to %s".formatted(currentStatus, targetStatus));
    }
}
