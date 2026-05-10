package com.kiniot.uflex.api.organization.domain.exceptions;

public class PhysiotherapistLicenseInvalidException extends RuntimeException {
    public PhysiotherapistLicenseInvalidException(String licenseNumber) {
        super("License number " + licenseNumber + " is invalid or already in use");
    }
}