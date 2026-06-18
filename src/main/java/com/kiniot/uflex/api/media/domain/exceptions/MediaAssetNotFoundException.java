package com.kiniot.uflex.api.media.domain.exceptions;

public class MediaAssetNotFoundException extends RuntimeException {
    public MediaAssetNotFoundException(String identifier) {
        super("Media asset not found with identifier: %s".formatted(identifier));
    }
}
