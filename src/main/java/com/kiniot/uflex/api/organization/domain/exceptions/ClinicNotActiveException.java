package com.kiniot.uflex.api.organization.domain.exceptions;

public class ClinicNotActiveException extends RuntimeException {
    public ClinicNotActiveException(String message) {
        super(message);
    }

    public ClinicNotActiveException() {
        super("Clinic is not active");
    }
}