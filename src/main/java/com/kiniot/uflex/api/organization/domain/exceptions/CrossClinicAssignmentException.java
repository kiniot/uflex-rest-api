package com.kiniot.uflex.api.organization.domain.exceptions;

public class CrossClinicAssignmentException extends RuntimeException {
    public CrossClinicAssignmentException() {
        super("Cannot assign patient to a physiotherapist from a different clinic");
    }
}