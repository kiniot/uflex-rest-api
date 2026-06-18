package com.kiniot.uflex.api.media.domain.exceptions;

public class MediaUploadNotConfirmableException extends RuntimeException {
    public MediaUploadNotConfirmableException(String identifier, String currentStatus) {
        super("Media asset %s cannot be confirmed from status %s. Only PENDING uploads can be confirmed."
                .formatted(identifier, currentStatus));
    }
}
