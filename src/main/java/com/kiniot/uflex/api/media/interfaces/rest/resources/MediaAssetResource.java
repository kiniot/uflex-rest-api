package com.kiniot.uflex.api.media.interfaces.rest.resources;

import java.util.Date;

/**
 * Representation of a media asset. {@code downloadUrl} is a short-lived signed
 * URL generated on each read (null while the asset is still PENDING).
 */
public record MediaAssetResource(
        String id,
        String ownerType,
        String ownerId,
        String mediaType,
        String status,
        String contentType,
        String originalFileName,
        Long sizeBytes,
        String downloadUrl,
        Date createdAt
) {
}
