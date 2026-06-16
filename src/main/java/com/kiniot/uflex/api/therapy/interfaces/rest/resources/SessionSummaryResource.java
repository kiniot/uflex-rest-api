package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record SessionSummaryResource(
        UUID sessionId,
        UUID patientId,
        Integer totalSeries,
        Integer completedSeries,
        Integer painLevel,
        Integer painReportsCount,
        Integer highPainReportsCount,
        Integer maxReportedPainLevel,
        Boolean requiresClinicalReview,
        Integer anomaliesDetected,
        Instant startedAt,
        Instant finalizedAt
) {}
