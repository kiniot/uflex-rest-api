package com.kiniot.uflex.api.organization.domain.exceptions;

public class PhysiotherapistHasAssignedPatientsException extends RuntimeException {
    public PhysiotherapistHasAssignedPatientsException(String physiotherapistId) {
        super("Physiotherapist %s cannot be deleted because patients are still assigned".formatted(physiotherapistId));
    }
}
