package com.kiniot.uflex.api.planning.domain.exceptions;

public class CurrentUserPatientProfileNotFoundException extends RuntimeException {
    public CurrentUserPatientProfileNotFoundException() {
        super("Authenticated user does not have a patient profile");
    }
}
