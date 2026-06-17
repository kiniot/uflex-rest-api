package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SerieDetailsResource(
        UUID serieId,
        UUID exerciseId,
        Integer targetRepetitions,
        Double minAngle,
        Double maxAngle,
        Integer durationSeconds,
        Integer restDurationSeconds,
        String status
) {}
