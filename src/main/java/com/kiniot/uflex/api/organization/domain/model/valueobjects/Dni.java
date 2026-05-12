package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Dni(
        @Column(nullable = false, length = 8)
        String dni
) {
    public Dni {
        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("DNI cannot be null or blank");
        }
        if (!dni.matches("^[0-9]{8}$")) {
            throw new IllegalArgumentException("DNI must be exactly 8 digits");
        }
    }

    public Dni() {
        this("");
    }
}