package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

/**
 * The kit serial number (kitSerial): the device's cross-service identity. By contract this equals
 * {@code Device.serialNumber} (device context) and the edge gateway's {@code device_id}; it is what
 * links a therapy session to its physical device and its forwarded measurements. It is not the
 * backend Device UUID and not a MAC address (see device-identity-contract).
 */
@Embeddable
public record KitSerial(
        @Column(nullable = false)
        String value
) implements Serializable {

    public KitSerial {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("kitSerial must not be null or blank");
        }
    }

    public static KitSerial of(String value) {
        return new KitSerial(value);
    }

    public static String toStringOrNull(KitSerial vo) {
        return vo == null ? null : vo.value();
    }
}
