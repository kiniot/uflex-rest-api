package com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections;

import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Scalar projection of a therapy session for the history read path.
 *
 * <p>Deliberately carries no collections: the session graph has two EAGER bags
 * ({@code routine} is a {@code @OneToOne}, {@code compensatoryMovements} is an EAGER {@code List}),
 * so fetching {@code routine.series} alongside them would trip {@code MultipleBagFetchException}.
 * The per-session aggregates are computed by separate {@code GROUP BY} queries and stitched in the
 * query service, which keeps the history read at a constant number of queries.
 *
 * @param createdAt fallback ordering key: a session cancelled during preparation never started, so
 *                  {@code startedAt} is null and ordering on it alone would be unstable
 */
public record TherapySessionHistoryRow(
        UUID sessionId,
        SessionStatus status,
        Instant startedAt,
        Instant finalizedAt,
        UUID treatmentPlanId,
        UUID planningRoutineId,
        Integer painLevel,
        Integer maxReportedPainLevel,
        Boolean requiresClinicalReview,
        Date createdAt
) {}
