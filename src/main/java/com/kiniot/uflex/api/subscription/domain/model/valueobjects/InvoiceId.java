package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record InvoiceId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {
    public InvoiceId {
        if (id == null) {
            throw new IllegalArgumentException("Invoice ID cannot be null");
        }
    }

    public InvoiceId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}
