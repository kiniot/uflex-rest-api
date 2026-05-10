package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PhotoUrl(
        @Column(length = 500)
        String value
) {
    public PhotoUrl {
        if (value != null && !value.isBlank() && !value.matches("^https?://.*")) {
            throw new IllegalArgumentException("Photo URL must be a valid HTTP(S) URL");
        }
    }

    public PhotoUrl() {
        this(null);
    }
}