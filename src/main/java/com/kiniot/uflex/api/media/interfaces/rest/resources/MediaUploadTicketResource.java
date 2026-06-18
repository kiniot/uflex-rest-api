package com.kiniot.uflex.api.media.interfaces.rest.resources;

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
 * @param status           current asset status (PENDING).
 */
public record MediaUploadTicketResource(
        String mediaAssetId,
        String bucket,
        String objectPath,
        String uploadUrl,
        String token,
        long expiresInSeconds,
        String status
) {
}
