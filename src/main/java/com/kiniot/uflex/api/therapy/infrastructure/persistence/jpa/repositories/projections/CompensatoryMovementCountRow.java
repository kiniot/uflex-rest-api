package com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections;

import java.util.UUID;

/**
 * Per-session compensatory-movement count. See {@link SerieRepetitionAggregateRow} for why this is
 * a query of its own.
 */
public record CompensatoryMovementCountRow(UUID sessionId, Long compensatoryMovementsDetected) {}
