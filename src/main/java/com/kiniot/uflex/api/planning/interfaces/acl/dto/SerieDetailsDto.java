package com.kiniot.uflex.api.planning.interfaces.acl.dto;

public record SerieDetailsDto(
        String exerciseId,
        Integer targetRepetitions,
        Double rangeOfMotion,
        String movementType,
        String bodyPart,
        Integer durationSeconds,
        Integer restDurationSeconds
) {}
