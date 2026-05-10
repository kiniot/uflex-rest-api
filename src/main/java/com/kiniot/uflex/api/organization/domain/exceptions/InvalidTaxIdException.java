package com.kiniot.uflex.api.organization.domain.exceptions;

public class InvalidTaxIdException extends RuntimeException {
    public InvalidTaxIdException(String taxId) {
        super("Tax ID " + taxId + " is invalid");
    }
}