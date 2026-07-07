package com.kiniot.uflex.api.media.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record MediaAssetId(
        @Column(columnDefinition = "UUID")
        UUID id
) {
    public MediaAssetId {
        if (id == null) {
            throw new IllegalArgumentException("Media asset ID cannot be null");
        }
    }

    public MediaAssetId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}
