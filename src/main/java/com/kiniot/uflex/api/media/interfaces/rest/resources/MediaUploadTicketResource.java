package com.kiniot.uflex.api.media.interfaces.rest.resources;

import java.util.Map;

/**
 * Response of the create-upload endpoint. The client must PUT the file bytes to
 * {@code uploadUrl} (with header Content-Type set to the same MIME type), then
 * call the confirm endpoint with {@code mediaAssetId}.
 *
 * @param mediaAssetId     id of the (PENDING) asset; used later to confirm/read/delete.
 * @param bucket           Supabase bucket the object will live in.
 * @param objectPath       object key inside the bucket.
 * @param uploadUrl        absolute URL to PUT the file to (token embedded).
 * @param token            upload token (for SDK-based uploads).
 * @param expiresInSeconds TTL of the upload URL.
 * @param preferredStrategy recommended upload strategy for the client.
 * @param resumableEndpoint TUS endpoint for resumable uploads.
 * @param resumableHeaders  headers required to create/continue the resumable upload.
 * @param resumableMetadata metadata required by Supabase TUS.
 * @param resumableChunkSizeBytes recommended chunk size for TUS uploads.
 * @param status           current asset status (PENDING).
 */
public record MediaUploadTicketResource(
        String mediaAssetId,
        String bucket,
        String objectPath,
        String uploadUrl,
        String token,
        long expiresInSeconds,
        String preferredStrategy,
        String resumableEndpoint,
        Map<String, String> resumableHeaders,
        Map<String, String> resumableMetadata,
        Long resumableChunkSizeBytes,
        String status
) {
}
