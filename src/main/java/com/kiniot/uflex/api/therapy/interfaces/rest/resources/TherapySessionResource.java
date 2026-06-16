package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record TherapySessionResource(
        UUID id,
        UUID patientId,
        UUID treatmentPlanId,
        String iotDeviceId,
        String status,
        Integer painLevel,
        Boolean requiresClinicalReview,
        Instant startedAt,
        Instant finalizedAt
) {}
