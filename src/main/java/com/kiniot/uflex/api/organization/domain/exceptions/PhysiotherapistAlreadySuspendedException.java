package com.kiniot.uflex.api.organization.domain.exceptions;

public class PhysiotherapistAlreadySuspendedException extends RuntimeException {
    public PhysiotherapistAlreadySuspendedException() {
        super("Physiotherapist profile is already suspended");
    }
}
