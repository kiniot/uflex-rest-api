package com.kiniot.uflex.api.organization.domain.exceptions;

public class SuspendedPhysiotherapistAssignmentException extends RuntimeException {
    public SuspendedPhysiotherapistAssignmentException() {
        super("Cannot assign or register patients to a suspended physiotherapist");
    }
}
