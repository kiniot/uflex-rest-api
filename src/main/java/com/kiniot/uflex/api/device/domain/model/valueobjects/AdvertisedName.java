package com.kiniot.uflex.api.device.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record AdvertisedName(
        @Column(length = 100)
        String advertisedName
) {
    public AdvertisedName {
        if (advertisedName != null && advertisedName.isBlank()) {
            advertisedName = null;
        }
    }

    public AdvertisedName() {
        this(null);
    }

    public String value() {
        return advertisedName;
    }
}
