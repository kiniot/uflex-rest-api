package com.kiniot.uflex.api.organization.domain.exceptions;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String message) {
        super(message);
    }
}
