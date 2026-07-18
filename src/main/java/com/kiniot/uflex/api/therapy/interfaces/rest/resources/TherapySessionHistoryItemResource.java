package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * One row of a patient's therapy-session history: enough to render the table and to plot a per-plan
 * ROM trend from {@code averageAchievedRom} over {@code startedAt}, without a request per session.
 *
 * @param startedAt           null for a session cancelled before it ever started
 * @param averageAchievedRom  null when the session recorded no repetitions
 */
@Builder
public record TherapySessionHistoryItemResource(
        UUID sessionId,
        String status,
        Instant startedAt,
        Instant finalizedAt,
        UUID treatmentPlanId,
        UUID planningRoutineId,
        Integer totalSeries,
        Integer completedSeries,
        Integer totalRepetitions,
        Integer goodRepetitions,
        Integer incompleteRepetitions,
        Integer unsafeRepetitions,
        Double averageAchievedRom,
        Integer painLevel,
        Integer maxReportedPainLevel,
        Boolean requiresClinicalReview,
        Integer compensatoryMovementsDetected
) {}
