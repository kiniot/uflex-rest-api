package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * One compensatory movement detected during the session. Hangs off the session rather than a
 * specific serie, so it can only be correlated to one by {@code detectedAt}.
 */
@Builder
public record CompensatoryMovementResource(UUID movementId, String type, Instant detectedAt) {}
