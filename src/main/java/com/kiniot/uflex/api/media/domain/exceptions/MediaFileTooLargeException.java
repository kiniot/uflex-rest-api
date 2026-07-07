package com.kiniot.uflex.api.media.domain.exceptions;

public class MediaFileTooLargeException extends RuntimeException {
    public MediaFileTooLargeException(long sizeBytes, long maxBytes) {
        super("Media file size %d bytes exceeds the maximum allowed of %d bytes".formatted(sizeBytes, maxBytes));
    }
}
