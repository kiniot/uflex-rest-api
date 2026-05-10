package com.kiniot.uflex.api.organization.domain.exceptions;

public class PatientAlreadyRegisteredException extends RuntimeException {
    public PatientAlreadyRegisteredException(String userId) {
        super("Patient profile already exists for user: " + userId);
    }
}