package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PhoneNumber(
        @Column(nullable = false, length = 15)
        String countryCode,
        @Column(nullable = false, length = 15)
        String number
) {
    public PhoneNumber {
        if (countryCode == null || countryCode.isBlank()) {
            throw new IllegalArgumentException("Country code cannot be null or blank");
        }
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }
        if (!isValidE164(countryCode, number)) {
            throw new IllegalArgumentException("Phone number must be in E.164 format");
        }
    }

    public PhoneNumber() {
        this("+51", "");
    }

    private static boolean isValidE164(String countryCode, String number) {
        String phone = countryCode + number;
        return phone.length() >= 8 && phone.length() <= 15;
    }

    public String formatted() {
        return countryCode + number;
    }
}