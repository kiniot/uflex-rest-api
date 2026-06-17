package com.kiniot.uflex.api.planning.domain.exceptions;

public class OverlappingTreatmentPlanPeriodException extends RuntimeException {
    public OverlappingTreatmentPlanPeriodException(String patientId) {
        super("Treatment plan period overlaps with another scheduled or active plan for patient " + patientId);
    }
}
