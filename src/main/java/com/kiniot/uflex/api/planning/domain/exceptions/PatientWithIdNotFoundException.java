package com.kiniot.uflex.api.planning.domain.exceptions;

public class PatientWithIdNotFoundException extends RuntimeException {
    public PatientWithIdNotFoundException(String patientId) {
        super("Patient with id " + patientId + " not found");
    }
}
