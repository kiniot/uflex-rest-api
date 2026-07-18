package com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections;

import java.util.UUID;

/**
 * Per-session aggregate over the {@code routine -> series -> repetitions} branch.
 *
 * <p>Kept separate from the compensatory-movement count on purpose: joining both collections in one
 * query produces a cartesian product that silently multiplies every count.
 */
public record SerieRepetitionAggregateRow(
        UUID sessionId,
        Long totalSeries,
        Long completedSeries,
        Long totalRepetitions,
        Long goodRepetitions,
        Long incompleteRepetitions,
        Long unsafeRepetitions,
        Double averageAchievedRom
) {}
