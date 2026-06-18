package com.kiniot.uflex.api.media.domain.model.valueobjects;

/**
 * Result of asking the storage provider for a signed upload URL. This is NOT a
 * persisted value object; it only travels from the storage port back to the
 * caller so the client can PUT the file directly to Supabase Storage.
 *
 * @param uploadUrl        absolute URL the client must PUT the file to (token already embedded).
 * @param token            the upload token (useful for SDK-based uploads, e.g. supabase-kt / supabase-js).
 * @param expiresInSeconds how long the signed upload URL stays valid.
 */
public record SignedUpload(
        String uploadUrl,
        String token,
        long expiresInSeconds
) {
}
