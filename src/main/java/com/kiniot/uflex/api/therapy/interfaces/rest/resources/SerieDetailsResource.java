package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SerieDetailsResource(
        UUID serieId,
        UUID exerciseId,
        Integer targetRepetitions,
        Double targetRom,
        String movementType,
        String bodyPart,
        Integer durationSeconds,
        Integer restDurationSeconds,
        String status
) {}
