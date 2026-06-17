package com.kiniot.uflex.api.device.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record FirmwareVersion(
        @Column(length = 32)
        String firmwareVersion
) {
    public FirmwareVersion {
        if (firmwareVersion != null && firmwareVersion.isBlank()) {
            firmwareVersion = null;
        }
    }

    public FirmwareVersion() {
        this(null);
    }

    public String value() {
        return firmwareVersion;
    }
}