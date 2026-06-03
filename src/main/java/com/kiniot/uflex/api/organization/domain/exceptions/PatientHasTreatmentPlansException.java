package com.kiniot.uflex.api.organization.domain.exceptions;

public class PatientHasTreatmentPlansException extends RuntimeException {
    public PatientHasTreatmentPlansException(String patientId) {
        super("Patient %s cannot be deleted because treatment plans still exist".formatted(patientId));
    }
}
