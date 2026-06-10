package com.kiniot.uflex.api.therapy.domain.exceptions;

public class PatientAlreadyInActiveSessionException extends RuntimeException {

    private PatientAlreadyInActiveSessionException(String message) {
        super(message);
    }

    public static PatientAlreadyInActiveSessionException forPatient(String patientId) {
        return new PatientAlreadyInActiveSessionException(
                "Patient with ID %s already has an active therapy session".formatted(patientId));
    }
}
