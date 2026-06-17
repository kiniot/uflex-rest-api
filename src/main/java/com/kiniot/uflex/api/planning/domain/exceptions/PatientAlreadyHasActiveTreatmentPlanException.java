package com.kiniot.uflex.api.planning.domain.exceptions;

public class PatientAlreadyHasActiveTreatmentPlanException extends RuntimeException {
    public PatientAlreadyHasActiveTreatmentPlanException(String patientId) {
        super("Patient " + patientId + " already has an active treatment plan");
    }
}
