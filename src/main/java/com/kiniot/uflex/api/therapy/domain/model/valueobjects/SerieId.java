package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public record SerieId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {

    public SerieId {
        Objects.requireNonNull(id, "serieId must not be null");
    }

    public SerieId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }

    public static SerieId of(UUID id) {
        Objects.requireNonNull(id, "serieId must not be null");
        return new SerieId(id);
    }

    public static SerieId fromNullable(UUID id) {
        return id == null ? null : new SerieId(id);
    }

    public static String toStringOrNull(SerieId vo) {
        return vo == null ? null : vo.id().toString();
    }
}
