package com.kiniot.uflex.api.media.domain.exceptions;

/**
 * Raised when the underlying storage provider (Supabase Storage) cannot complete
 * an operation (signing a URL, deleting an object, etc.). Mapped to 502 Bad Gateway.
 */
public class MediaStorageException extends RuntimeException {
    public MediaStorageException(String message) {
        super(message);
    }

    public MediaStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
