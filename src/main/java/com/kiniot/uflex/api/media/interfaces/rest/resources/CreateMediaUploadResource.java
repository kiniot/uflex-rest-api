package com.kiniot.uflex.api.media.interfaces.rest.resources;

/**
 * Request body to start an upload (step 1 of the signed-URL flow).
 *
 * @param ownerType   PHYSIOTHERAPIST_RECORD | PATIENT_EVIDENCE | PROFILE_PHOTO | GENERIC.
 * @param ownerId     UUID of the owner entity (optional for GENERIC).
 * @param mediaType   IMAGE | VIDEO.
 * @param contentType MIME type of the file to upload (e.g. "image/jpeg", "video/mp4").
 * @param fileName    original file name (optional).
 * @param sizeBytes   declared file size in bytes (optional, validated against limits).
 */
public record CreateMediaUploadResource(
        String ownerType,
        String ownerId,
        String mediaType,
        String contentType,
        String fileName,
        Long sizeBytes
) {
}
