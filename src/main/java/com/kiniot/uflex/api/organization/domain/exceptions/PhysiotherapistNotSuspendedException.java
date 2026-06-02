package com.kiniot.uflex.api.organization.domain.exceptions;

public class PhysiotherapistNotSuspendedException extends RuntimeException {
    public PhysiotherapistNotSuspendedException() {
        super("Physiotherapist profile is not suspended");
    }
}
