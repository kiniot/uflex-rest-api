package com.kiniot.uflex.api.organization.domain.exceptions;

public class PatientAlreadyRegisteredException extends RuntimeException {
    public PatientAlreadyRegisteredException(String identifier) {
        super("Patient profile already exists for: " + identifier);
    }
}
