package com.kiniot.uflex.api.organization.domain.exceptions;

public class PatientAccessDeniedException extends RuntimeException {
    public PatientAccessDeniedException(String message) {
        super(message);
    }
}
