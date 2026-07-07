package com.kiniot.uflex.api.planning.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.media.interfaces.acl.MediaContextFacade;
import com.kiniot.uflex.api.media.interfaces.acl.dto.MediaAssetDto;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("planningExternalMediaService")
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

    public void assignExerciseVideo(UUID mediaAssetId, UUID exerciseId) {
        mediaContextFacade.assignMediaAsset(
                mediaAssetId.toString(),
                "EXERCISE_VIDEO",
                exerciseId.toString()
        );
    }

    public String createSignedDownloadUrl(UUID mediaAssetId) {
        if (mediaAssetId == null) {
            return null;
        }
        return mediaContextFacade.createSignedDownloadUrl(mediaAssetId.toString());
    }
}
