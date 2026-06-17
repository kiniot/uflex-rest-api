package com.kiniot.uflex.api.planning.interfaces.acl.dto;

public record SerieDetailsDto(
        String exerciseId,
        Integer targetRepetitions,
        Double rangeOfMotion,
        Integer durationSeconds,
        Integer restDurationSeconds
) {}
