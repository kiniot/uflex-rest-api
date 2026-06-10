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
        Instant startedAt,
        Instant finalizedAt
) {}
