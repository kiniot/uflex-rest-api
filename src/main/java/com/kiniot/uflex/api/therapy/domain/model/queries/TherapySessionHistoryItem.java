package com.kiniot.uflex.api.therapy.domain.model.queries;

import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-model row of {@link GetTherapySessionHistoryQuery}: one finished-or-running session with its
 * aggregates already computed, enough to render a history table and a per-plan ROM trend without a
 * follow-up request per session.
 *
 * @param averageAchievedRom null when the session recorded no repetitions
 * @param painLevel          the last reported level; the model keeps no pain history, so this is a
 *                           point reading rather than a curve
 */
public record TherapySessionHistoryItem(
        UUID sessionId,
        SessionStatus status,
        Instant startedAt,
        Instant finalizedAt,
        UUID treatmentPlanId,
        UUID planningRoutineId,
        int totalSeries,
        int completedSeries,
        int totalRepetitions,
        int goodRepetitions,
        int incompleteRepetitions,
        int unsafeRepetitions,
        Double averageAchievedRom,
        Integer painLevel,
        Integer maxReportedPainLevel,
        Boolean requiresClinicalReview,
        int compensatoryMovementsDetected
) {}
