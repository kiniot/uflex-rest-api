package com.kiniot.uflex.api.organization.domain.exceptions;

public class ClinicAlreadyRegisteredException extends RuntimeException {
    public ClinicAlreadyRegisteredException(String taxId) {
        super("Clinic with RUC " + taxId + " is already registered");
    }
}