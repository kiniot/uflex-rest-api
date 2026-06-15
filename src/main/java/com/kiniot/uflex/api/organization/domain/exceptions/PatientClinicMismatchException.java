package com.kiniot.uflex.api.organization.domain.exceptions;

public class PatientClinicMismatchException extends RuntimeException {
    public PatientClinicMismatchException() {
        super("Patient does not belong to the authenticated clinic");
    }
}
