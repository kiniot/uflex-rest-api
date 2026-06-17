package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Daily schedule projection for a patient. {@code resolutionStatus} explains
 * whether a routine was found for the requested date. When no routine is
 * available, {@code routineId} is null and the counters are zero.
 */
public record DailyScheduleResource(
        UUID patientId,
        LocalDate date,
        String resolutionStatus,
        UUID routineId,
        Integer totalSeries,
        Integer estimatedDurationMinutes
) {}
