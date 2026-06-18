package com.kiniot.uflex.api.media.interfaces.rest.resources;

/**
 * Request body to confirm an upload succeeded (step 3 of the signed-URL flow).
 *
 * @param sizeBytes actual uploaded size in bytes (optional).
 */
public record ConfirmMediaUploadResource(
        Long sizeBytes
) {
}
