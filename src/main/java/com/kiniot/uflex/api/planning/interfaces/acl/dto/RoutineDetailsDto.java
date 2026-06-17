package com.kiniot.uflex.api.planning.interfaces.acl.dto;

import java.util.List;

public record RoutineDetailsDto(
        String routineId,
        List<SerieDetailsDto> series
) {}
