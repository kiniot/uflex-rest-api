package com.kiniot.uflex.api.organization.domain.exceptions;

public class PhysiotherapistAlreadyRegisteredException extends RuntimeException {
    public PhysiotherapistAlreadyRegisteredException(String userId) {
        super("Physiotherapist already registered for user: " + userId);
    }
}