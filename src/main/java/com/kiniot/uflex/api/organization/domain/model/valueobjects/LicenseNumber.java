package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record LicenseNumber(
        @Column(nullable = false, unique = true, length = 15)
        String value
) {
    public LicenseNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("License number cannot be null or blank");
        }
        if (!isValidFormat(value)) {
            throw new IllegalArgumentException("License number must be in CMP or CTTMP format");
        }
    }

    public LicenseNumber() {
        this("");
    }

    private static boolean isValidFormat(String value) {
        return value.matches("^\\d{4,8}[A-Z]{2,4}$") ||
               value.matches("^[A-Z]{2,4}\\d{4,8}$");
    }
}