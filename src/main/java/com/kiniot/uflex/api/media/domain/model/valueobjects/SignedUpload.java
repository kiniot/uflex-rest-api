package com.kiniot.uflex.api.media.domain.model.valueobjects;

import java.util.Map;

/**
 * Result of asking the storage provider for a signed upload URL. This is NOT a
 * persisted value object; it only travels from the storage port back to the
 * caller so the client can PUT the file directly to Supabase Storage.
 *
 * @param uploadUrl        absolute URL the client must PUT the file to (token already embedded).
 * @param token            the upload token (useful for SDK-based uploads, e.g. supabase-kt / supabase-js).
 * @param expiresInSeconds how long the signed upload URL stays valid.
 * @param preferredStrategy recommended upload strategy for the client (`SIMPLE_PUT` or `TUS_RESUMABLE`).
 * @param resumableEndpoint TUS endpoint for resumable uploads.
 * @param resumableHeaders  headers required to create/continue the resumable upload.
 * @param resumableMetadata metadata the client should send to Supabase TUS.
 * @param resumableChunkSizeBytes recommended TUS chunk size in bytes.
 */
public record SignedUpload(
        String uploadUrl,
        String token,
        long expiresInSeconds,
        String preferredStrategy,
        String resumableEndpoint,
        Map<String, String> resumableHeaders,
        Map<String, String> resumableMetadata,
        Long resumableChunkSizeBytes
) {
}
