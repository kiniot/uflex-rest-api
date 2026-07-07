package com.kiniot.uflex.api.organization.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.media.interfaces.acl.MediaContextFacade;
import com.kiniot.uflex.api.media.interfaces.acl.dto.MediaAssetDto;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("organizationExternalMediaService")
public class ExternalMediaService {

    private final MediaContextFacade mediaContextFacade;

    public ExternalMediaService(MediaContextFacade mediaContextFacade) {
        this.mediaContextFacade = mediaContextFacade;
    }

    public Optional<MediaAssetDto> findMediaAssetById(UUID mediaAssetId) {
        if (mediaAssetId == null) {
            return Optional.empty();
        }
        return mediaContextFacade.findMediaAssetById(mediaAssetId.toString());
    }

    public void assignProfilePhoto(UUID mediaAssetId, UUID physiotherapistId) {
        mediaContextFacade.assignMediaAsset(
                mediaAssetId.toString(),
                "PROFILE_PHOTO",
                physiotherapistId.toString()
        );
    }

    public String createSignedDownloadUrl(UUID mediaAssetId) {
        if (mediaAssetId == null) {
            return null;
        }
        return mediaContextFacade.createSignedDownloadUrl(mediaAssetId.toString());
    }
}
