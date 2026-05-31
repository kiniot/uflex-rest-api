package com.kiniot.uflex.api.shared.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Embeddable
public record Address(
        @Column(nullable = false, length = 2)
        String countryCode,
        @Column(nullable = false, length = 100)
        String region,
        @Column(nullable = false, length = 100)
        String city,
        @Column(nullable = false, length = 300)
        String addressLine1,
        @Column(length = 300)
        String addressLine2,
        @Column(length = 20)
        String postalCode
) {
    private static final Set<String> ISO_COUNTRY_CODES = Arrays.stream(Locale.getISOCountries())
            .collect(Collectors.toUnmodifiableSet());

    public Address {
        countryCode = normalizeRequired(countryCode, "Country code");
        countryCode = countryCode.toUpperCase(Locale.ROOT);
        if (countryCode.length() != 2 || !ISO_COUNTRY_CODES.contains(countryCode)) {
            throw new IllegalArgumentException("Country code must be a valid ISO 3166-1 alpha-2 code");
        }

        region = normalizeRequired(region, "Region");
        validateMaxLength(region, 100, "Region");

        city = normalizeRequired(city, "City");
        validateMaxLength(city, 100, "City");

        addressLine1 = normalizeRequired(addressLine1, "Address line 1");
        validateMaxLength(addressLine1, 300, "Address line 1");

        addressLine2 = normalizeOptional(addressLine2);
        validateMaxLength(addressLine2, 300, "Address line 2");

        postalCode = normalizeOptional(postalCode);
        validateMaxLength(postalCode, 20, "Postal code");
    }

    public Address() {
        this("", "", "", "", null, null);
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLength + " characters");
        }
    }
}
