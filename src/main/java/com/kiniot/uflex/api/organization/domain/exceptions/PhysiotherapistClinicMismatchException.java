package com.kiniot.uflex.api.organization.domain.exceptions;

public class PhysiotherapistClinicMismatchException extends RuntimeException {
    public PhysiotherapistClinicMismatchException() {
        super("Physiotherapist does not belong to the authenticated clinic");
    }
}
