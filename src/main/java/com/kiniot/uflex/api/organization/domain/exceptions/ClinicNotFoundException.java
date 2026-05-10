package com.kiniot.uflex.api.organization.domain.exceptions;

public class ClinicNotFoundException extends RuntimeException {
    public ClinicNotFoundException(String message) {
        super(message);
    }
}