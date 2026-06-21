package com.kiniot.uflex.api.device.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record AdvertisedName(
        @Column(length = 100)
        String advertisedName
) {
    // BLE broadcast name; kept short enough to fit a scan-response advertisement
    // (31 bytes minus the 2-byte AD header). See device-identity-contract.
    public static final int MAX_LENGTH = 26;

    public AdvertisedName {
        if (advertisedName != null && advertisedName.isBlank()) {
            advertisedName = null;
        }
        if (advertisedName != null && advertisedName.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Advertised name must be at most " + MAX_LENGTH + " characters");
        }
    }

    public AdvertisedName() {
        this(null);
    }

    public String value() {
        return advertisedName;
    }
}
