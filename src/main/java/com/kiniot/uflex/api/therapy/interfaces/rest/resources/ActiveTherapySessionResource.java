package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Active-session view for the edge: the session fields plus its series with their targets
 * (targetRom, movementType, bodyPart), so the edge can correlate the kit serial with the
 * active session/serie and read the parameters it needs for detection in a single call.
 */
@Builder
public record ActiveTherapySessionResource(
        UUID id,
        UUID patientId,
        UUID treatmentPlanId,
        String iotDeviceId,
        Boolean sensorsPlaced,
        String status,
        Integer painLevel,
        Boolean requiresClinicalReview,
        Instant startedAt,
        Instant finalizedAt,
        List<SerieDetailsResource> series
) {}
