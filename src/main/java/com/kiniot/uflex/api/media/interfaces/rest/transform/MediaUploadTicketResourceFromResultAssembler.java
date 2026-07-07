package com.kiniot.uflex.api.media.interfaces.rest.transform;

import com.kiniot.uflex.api.media.domain.model.results.MediaUploadTicket;
import com.kiniot.uflex.api.media.interfaces.rest.resources.MediaUploadTicketResource;

public class MediaUploadTicketResourceFromResultAssembler {

    private MediaUploadTicketResourceFromResultAssembler() {}

    public static MediaUploadTicketResource toResourceFromResult(MediaUploadTicket ticket) {
        var asset = ticket.asset();
        var signed = ticket.signedUpload();
        return new MediaUploadTicketResource(
                asset.getId().id().toString(),
                asset.getBucket(),
                asset.getObjectPath(),
                signed.uploadUrl(),
                signed.token(),
                signed.expiresInSeconds(),
                signed.preferredStrategy(),
                signed.resumableEndpoint(),
                signed.resumableHeaders(),
                signed.resumableMetadata(),
                signed.resumableChunkSizeBytes(),
                asset.getStatus().name()
        );
    }
}
