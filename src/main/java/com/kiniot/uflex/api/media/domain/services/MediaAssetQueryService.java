package com.kiniot.uflex.api.media.domain.services;

import com.kiniot.uflex.api.media.domain.model.aggregates.MediaAsset;
import com.kiniot.uflex.api.media.domain.model.queries.GetMediaAssetByIdQuery;
import com.kiniot.uflex.api.media.domain.model.queries.GetMediaAssetsByOwnerQuery;

import java.util.List;
import java.util.Optional;

public interface MediaAssetQueryService {
    Optional<MediaAsset> handle(GetMediaAssetByIdQuery query);
    List<MediaAsset> handle(GetMediaAssetsByOwnerQuery query);
}
