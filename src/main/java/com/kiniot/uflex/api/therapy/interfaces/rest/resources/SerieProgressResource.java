package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SerieProgressResource(
        UUID serieId,
        UUID exerciseId,
        Integer currentRepetitions,
        Integer targetRepetitions,
        String status
) {}
