package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record SessionProgressResource(
        UUID sessionId,
        String status,
        UUID currentSerieId,
        Integer completedSeries,
        Integer totalSeries,
        Integer painLevel,
        Boolean requiresClinicalReview,
        List<SerieProgressResource> seriesProgress
) {}
