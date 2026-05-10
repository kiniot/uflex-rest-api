package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PhotoUrl(
        @Column(length = 500)
        String url
) {
    public PhotoUrl {
        if (url != null && !url.isBlank() && !url.matches("^https?://.*")) {
            throw new IllegalArgumentException("Photo URL must be a valid HTTP(S) URL");
        }
    }

    public PhotoUrl() {
        this(null);
    }
}