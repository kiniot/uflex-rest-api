package com.kiniot.uflex.api.media.domain.exceptions;

public class UnsupportedMediaContentTypeException extends RuntimeException {
    public UnsupportedMediaContentTypeException(String contentType) {
        super("Unsupported or mismatched media content type: %s".formatted(contentType));
    }
}
