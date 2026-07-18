package com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections;

import java.time.Instant;
import java.util.UUID;

/**
 * Per-patient therapy aggregate for the clinician's index.
 *
 * <p>Covers only the session/series/repetition branch. Compensatory movements are counted by a
 * separate query for the same reason as in the history read: joining both collections at once
 * multiplies every count through a cartesian product.
 */
public record PatientTherapyOverviewRow(
        UUID patientId,
        Long totalSessions,
        Long completedSessions,
        Long sessionsRequiringReview,
        Instant lastSessionAt,
        Long totalRepetitions,
        Long goodRepetitions,
        Double averageAchievedRom
) {}
