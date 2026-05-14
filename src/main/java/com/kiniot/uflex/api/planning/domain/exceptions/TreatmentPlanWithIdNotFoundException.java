package com.kiniot.uflex.api.planning.domain.exceptions;

public class TreatmentPlanWithIdNotFoundException extends RuntimeException {
    public TreatmentPlanWithIdNotFoundException(String treatmentPlanId) {
        super("Treatment plan with ID %s not found".formatted(treatmentPlanId));
    }
}
