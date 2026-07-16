package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full inspection view of one session for the clinic web: the aggregates of
 * {@link SessionSummaryResource} plus the series and every repetition underneath them.
 *
 * <p>Deliberately a different contract from {@code SessionSummaryResource} over the same aggregate:
 * the summary is the patient-facing final clinical record and only resolves once the session is
 * Completed/Cancelled, whereas this resolves in any status so a clinician can drill into a session
 * that is still running.
 *
 * @param painLevel the last reported level. The model overwrites pain on each report and keeps no
 *                  timestamps, so there is no intra-session pain curve to expose here.
 */
@Builder
public record TherapySessionDetailResource(
        UUID sessionId,
        UUID patientId,
        UUID treatmentPlanId,
        UUID planningRoutineId,
        String iotDeviceId,
        String status,
        Boolean sensorsPlaced,
        Instant startedAt,
        Instant finalizedAt,
        String cancellationReason,
        Integer totalSeries,
        Integer completedSeries,
        Integer totalRepetitions,
        Integer goodRepetitions,
        Integer incompleteRepetitions,
        Integer unsafeRepetitions,
        Double averageAchievedRom,
        Integer painLevel,
        Integer painReportsCount,
        Integer highPainReportsCount,
        Integer maxReportedPainLevel,
        Boolean requiresClinicalReview,
        Integer compensatoryMovementsDetected,
        List<SerieExecutionResource> series,
        List<CompensatoryMovementResource> compensatoryMovements
) {}
