package com.kiniot.uflex.api.media.domain.model.queries;

import com.kiniot.uflex.api.media.domain.model.valueobjects.OwnerType;

import java.util.UUID;

/**
 * Lists the UPLOADED media assets attached to a given owner within the
 * authenticated clinic.
 */
public record GetMediaAssetsByOwnerQuery(
        OwnerType ownerType,
        UUID ownerId
) {
    public GetMediaAssetsByOwnerQuery {
        if (ownerType == null) {
            throw new IllegalArgumentException("Owner type cannot be null");
        }
    }
}
