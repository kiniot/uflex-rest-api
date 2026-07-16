package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * One patient's therapy standing, for the clinician's index.
 *
 * @param lastSessionAt      null when the patient has never started a session
 * @param averageAchievedRom null when no repetition was ever recorded
 */
@Builder
public record PatientTherapyOverviewResource(
        UUID patientId,
        String patientFullName,
        Integer totalSessions,
        Integer completedSessions,
        Integer sessionsRequiringReview,
        Instant lastSessionAt,
        Integer totalRepetitions,
        Integer goodRepetitions,
        Double averageAchievedRom,
        Boolean hasActiveSession
) {}
