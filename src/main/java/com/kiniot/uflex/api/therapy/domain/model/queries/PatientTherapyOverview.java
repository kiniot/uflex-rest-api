package com.kiniot.uflex.api.therapy.domain.model.queries;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-model row of {@link GetPatientTherapyOverviewQuery}: one patient's therapy at a glance.
 *
 * <p>Patients with no sessions are included with zeroes rather than dropped: "assigned but never
 * started" is exactly what a clinician needs to spot.
 *
 * <p>Deliberately shallow — compensatory movements and per-session data belong to the drill-down.
 * This is the list you scan to decide who to look at.
 *
 * @param lastSessionAt      null when the patient has never started a session
 * @param averageAchievedRom null when no repetition was ever recorded
 */
public record PatientTherapyOverview(
        UUID patientId,
        String patientFullName,
        int totalSessions,
        int completedSessions,
        int sessionsRequiringReview,
        Instant lastSessionAt,
        int totalRepetitions,
        int goodRepetitions,
        Double averageAchievedRom,
        boolean hasActiveSession
) {}
