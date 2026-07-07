package com.kiniot.uflex.api.media.domain.model.valueobjects;

/**
 * The kind of media that an asset stores. Used to apply different validation
 * rules (allowed MIME types, size limits) on the client and the storage layer.
 */
public enum MediaType {
    IMAGE,
    VIDEO
}
