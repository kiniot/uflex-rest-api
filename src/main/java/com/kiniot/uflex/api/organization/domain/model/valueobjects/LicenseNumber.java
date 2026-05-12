package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record LicenseNumber(
        @Column(nullable = false, unique = true, length = 15)
        String licenseNumber
) {
    public LicenseNumber {
        if (licenseNumber == null || licenseNumber.isBlank()) {
            throw new IllegalArgumentException("License number cannot be null or blank");
        }
        if (!isValidFormat(licenseNumber)) {
            throw new IllegalArgumentException("License number must be in CMP or CTTMP format");
        }
    }

    private static boolean isValidFormat(String licenseNumber) {
        return licenseNumber.matches("^\\d{4,8}[A-Z]{2,4}$") ||
               licenseNumber.matches("^[A-Z]{2,4}\\d{4,8}$");
    }

    public LicenseNumber() {
        this("");
    }
}