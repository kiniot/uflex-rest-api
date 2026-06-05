package com.kiniot.uflex.api.device.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record SerialNumber(
        @Column(nullable = false, unique = true, length = 64)
        String serialNumber
) {
    public SerialNumber {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number cannot be null or empty");
        }
    }

    public SerialNumber() {
        this("");
    }

    public String value() {
        return serialNumber;
    }
}