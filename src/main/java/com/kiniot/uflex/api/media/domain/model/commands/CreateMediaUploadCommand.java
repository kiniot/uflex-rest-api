package com.kiniot.uflex.api.media.domain.model.commands;

import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaType;
import com.kiniot.uflex.api.media.domain.model.valueobjects.OwnerType;

import java.util.UUID;

/**
 * Request to start an upload. The backend will create a PENDING asset and ask
 * Supabase Storage for a signed upload URL.
 *
 * @param ownerType        what the media is attached to.
 * @param ownerId          id of the owner (may be null for GENERIC).
 * @param mediaType        IMAGE or VIDEO.
 * @param contentType      MIME type reported by the client (e.g. image/jpeg, video/mp4).
 * @param originalFileName original file name (optional, kept for display/download).
 * @param sizeBytes        declared size in bytes (optional, validated against limits).
 */
public record CreateMediaUploadCommand(
        OwnerType ownerType,
        UUID ownerId,
        MediaType mediaType,
        String contentType,
        String originalFileName,
        Long sizeBytes
) {
    public CreateMediaUploadCommand {
        if (ownerType == null) {
            throw new IllegalArgumentException("Owner type cannot be null");
        }
        if (mediaType == null) {
            throw new IllegalArgumentException("Media type cannot be null");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type cannot be null or blank");
        }
    }
}
