package com.kiniot.uflex.api.device.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record DeviceId(
        @Column(columnDefinition = "UUID")
        UUID id
) {
    public DeviceId {
        if (id == null) {
            throw new IllegalArgumentException("Device ID cannot be null");
        }
    }

    public DeviceId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}