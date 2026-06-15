package com.kiniot.uflex.api.organization.domain.exceptions;

public class PatientOperationNotAllowedException extends RuntimeException {
    public PatientOperationNotAllowedException(String message) {
        super(message);
    }
}
