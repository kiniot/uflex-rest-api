package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One repetition as detected by the edge.
 *
 * @param peakAngle   peak joint angle reached (degrees)
 * @param achievedRom peak minus the baseline discovered for that repetition
 * @param recordedAt  edge-supplied wall clock, a {@code LocalDateTime} with no zone — do not put it
 *                    on a shared time axis with the session's {@code Instant} fields
 */
@Builder
public record CompletedRepetitionResource(
        UUID repetitionId,
        Double peakAngle,
        Double achievedRom,
        String classification,
        LocalDateTime recordedAt
) {}
