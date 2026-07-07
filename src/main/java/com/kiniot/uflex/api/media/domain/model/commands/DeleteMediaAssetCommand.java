package com.kiniot.uflex.api.media.domain.model.commands;

import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaAssetId;

public record DeleteMediaAssetCommand(
        MediaAssetId mediaAssetId
) {
    public DeleteMediaAssetCommand {
        if (mediaAssetId == null) {
            throw new IllegalArgumentException("Media asset ID cannot be null");
        }
    }
}
