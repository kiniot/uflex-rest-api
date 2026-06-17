package com.kiniot.uflex.api.device.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record DeviceModel(
        @Column(length = 100)
        String modelName
) {
    public DeviceModel {
        if (modelName != null && modelName.isBlank()) {
            modelName = null;
        }
    }

    public DeviceModel() {
        this(null);
    }

    public String value() {
        return modelName;
    }
}