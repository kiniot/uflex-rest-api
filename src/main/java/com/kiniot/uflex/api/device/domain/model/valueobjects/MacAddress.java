package com.kiniot.uflex.api.device.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record MacAddress(
        @Column(nullable = false, unique = true, length = 17)
        String macAddress
) {
    private static final Pattern MAC_PATTERN = Pattern.compile(
            "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$"
    );

    public MacAddress {
        if (macAddress == null || macAddress.isBlank()) {
            throw new IllegalArgumentException("MAC address cannot be null or empty");
        }
        if (!MAC_PATTERN.matcher(macAddress).matches()) {
            throw new IllegalArgumentException("Invalid MAC address format. Expected XX:XX:XX:XX:XX:XX");
        }
    }

    public MacAddress() {
        this("");
    }

    public String value() {
        return macAddress;
    }
}