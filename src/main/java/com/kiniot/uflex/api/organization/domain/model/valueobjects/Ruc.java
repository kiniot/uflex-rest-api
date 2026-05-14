package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Ruc(
        @Column(nullable = false, unique = true, length = 11)
        String ruc
) {
    public Ruc {
        if (ruc == null || ruc.isBlank()) {
            throw new IllegalArgumentException("RUC cannot be null or blank");
        }
        if (!ruc.matches("\\d{11}")) {
            throw new IllegalArgumentException("RUC must be exactly 11 digits");
        }
//        if (!isValidRuc(ruc)) {
//            throw new IllegalArgumentException("RUC has invalid checksum digit");
//        }
    }

    public Ruc() {
        this("");
    }

    private static boolean isValidRuc(String ruc) {
        int[] multipliers = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(ruc.charAt(i)) * multipliers[i];
        }
        int remainder = sum % 11;
        int checkDigit = 11 - remainder;
        if (checkDigit == 10) checkDigit = 0;
        if (checkDigit == 11) checkDigit = 1;
        return checkDigit == Character.getNumericValue(ruc.charAt(10));
    }
}