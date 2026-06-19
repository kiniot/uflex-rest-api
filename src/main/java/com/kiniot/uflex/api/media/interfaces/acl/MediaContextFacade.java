package com.kiniot.uflex.api.media.interfaces.acl;

import com.kiniot.uflex.api.media.interfaces.acl.dto.MediaAssetDto;

import java.util.Optional;

public interface MediaContextFacade {
    Optional<MediaAssetDto> findMediaAssetById(String mediaAssetId);

    void assignMediaAsset(String mediaAssetId, String ownerType, String ownerId);

    String createSignedDownloadUrl(String mediaAssetId);
}
