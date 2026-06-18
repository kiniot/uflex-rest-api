package com.kiniot.uflex.api.media.domain.model.queries;

import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaAssetId;

public record GetMediaAssetByIdQuery(
        MediaAssetId mediaAssetId
) {
    public GetMediaAssetByIdQuery {
        if (mediaAssetId == null) {
            throw new IllegalArgumentException("Media asset ID cannot be null");
        }
    }
}
