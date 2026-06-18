package com.kiniot.uflex.api.media.domain.model.valueobjects;

/**
 * Lifecycle of a media asset under the signed-URL upload flow.
 * <ul>
 *     <li>{@code PENDING}: the row exists and a signed upload URL was issued, but the
 *     client has not confirmed the upload to Supabase Storage yet.</li>
 *     <li>{@code UPLOADED}: the client confirmed the file is stored in Supabase Storage.</li>
 *     <li>{@code FAILED}: the upload was reported as failed and the asset is unusable.</li>
 * </ul>
 */
public enum MediaStatus {
    PENDING,
    UPLOADED,
    FAILED
}
