package com.kiniot.uflex.api.media.domain.model.results;

import com.kiniot.uflex.api.media.domain.model.aggregates.MediaAsset;
import com.kiniot.uflex.api.media.domain.model.valueobjects.SignedUpload;

/**
 * Returned by the command service when a client requests an upload. It bundles
 * the persisted (PENDING) asset together with the transient signed upload data
 * so the controller can hand the client everything it needs to upload directly
 * to Supabase Storage.
 */
public record MediaUploadTicket(
        MediaAsset asset,
        SignedUpload signedUpload
) {
}
