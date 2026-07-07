package com.kiniot.uflex.api.iam.domain.exceptions;

/**
 * Raised when attempting to provision an edge service account for a kit serial that
 * already has one.
 */
public class EdgeServiceAccountAlreadyExistsException extends RuntimeException {
    public EdgeServiceAccountAlreadyExistsException(String serialNumber) {
        super("An edge service account already exists for serial number: " + serialNumber);
    }
}
