package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public record TherapySessionId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {

    public TherapySessionId {
        Objects.requireNonNull(id, "sessionId must not be null");
    }

    public TherapySessionId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }

    public static TherapySessionId of(UUID id) {
        Objects.requireNonNull(id, "sessionId must not be null");
        return new TherapySessionId(id);
    }

    public static TherapySessionId fromNullable(UUID id) {
        return id == null ? null : new TherapySessionId(id);
    }

    public static String toStringOrNull(TherapySessionId vo) {
        return vo == null ? null : vo.id().toString();
    }
}
