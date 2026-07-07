package com.kiniot.uflex.api.media.interfaces.rest.transform;

import com.kiniot.uflex.api.media.domain.model.aggregates.MediaAsset;
import com.kiniot.uflex.api.media.interfaces.rest.resources.MediaAssetResource;

public class MediaAssetResourceFromEntityAssembler {

    private MediaAssetResourceFromEntityAssembler() {}

    public static MediaAssetResource toResourceFromEntity(MediaAsset entity, String downloadUrl) {
        return new MediaAssetResource(
                entity.getId().id().toString(),
                entity.getOwnerType().name(),
                entity.getOwnerId() != null ? entity.getOwnerId().toString() : null,
                entity.getMediaType().name(),
                entity.getStatus().name(),
                entity.getContentType(),
                entity.getOriginalFileName(),
                entity.getSizeBytes(),
                downloadUrl,
                entity.getCreatedAt()
        );
    }
}
