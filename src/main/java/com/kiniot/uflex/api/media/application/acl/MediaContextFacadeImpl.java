package com.kiniot.uflex.api.media.application.acl;

import com.kiniot.uflex.api.media.domain.exceptions.MediaAssetNotFoundException;
import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaAssetId;
import com.kiniot.uflex.api.media.domain.model.valueobjects.OwnerType;
import com.kiniot.uflex.api.media.domain.services.MediaStorageService;
import com.kiniot.uflex.api.media.infrastructure.persistence.jpa.repositories.MediaAssetRepository;
import com.kiniot.uflex.api.media.infrastructure.storage.supabase.configuration.SupabaseStorageProperties;
import com.kiniot.uflex.api.media.interfaces.acl.MediaContextFacade;
import com.kiniot.uflex.api.media.interfaces.acl.dto.MediaAssetDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class MediaContextFacadeImpl implements MediaContextFacade {

    private final MediaAssetRepository mediaAssetRepository;
    private final MediaStorageService mediaStorageService;
    private final SupabaseStorageProperties properties;

    public MediaContextFacadeImpl(
            MediaAssetRepository mediaAssetRepository,
            MediaStorageService mediaStorageService,
            SupabaseStorageProperties properties
    ) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.mediaStorageService = mediaStorageService;
        this.properties = properties;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaAssetDto> findMediaAssetById(String mediaAssetId) {
        return mediaAssetRepository.findById(new MediaAssetId(UUID.fromString(mediaAssetId)))
                .map(asset -> new MediaAssetDto(
                        asset.getId().id().toString(),
                        asset.getClinicId().id().toString(),
                        asset.getOwnerType().name(),
                        asset.getOwnerId() != null ? asset.getOwnerId().toString() : null,
                        asset.getMediaType().name(),
                        asset.getStatus().name(),
                        asset.getContentType(),
                        asset.getOriginalFileName(),
                        asset.getSizeBytes()
                ));
    }

    @Override
    @Transactional
    public void assignMediaAsset(String mediaAssetId, String ownerType, String ownerId) {
        var asset = mediaAssetRepository.findById(new MediaAssetId(UUID.fromString(mediaAssetId)))
                .orElseThrow(() -> new MediaAssetNotFoundException(mediaAssetId));
        asset.assignOwner(OwnerType.valueOf(ownerType), ownerId != null ? UUID.fromString(ownerId) : null);
        mediaAssetRepository.save(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public String createSignedDownloadUrl(String mediaAssetId) {
        return mediaAssetRepository.findById(new MediaAssetId(UUID.fromString(mediaAssetId)))
                .map(asset -> mediaStorageService.createSignedDownloadUrl(
                        asset.getBucket(),
                        asset.getObjectPath(),
                        properties.getDownloadUrlExpirySeconds()))
                .orElse(null);
    }
}
