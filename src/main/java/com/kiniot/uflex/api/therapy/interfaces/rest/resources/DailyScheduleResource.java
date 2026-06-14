package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Daily schedule projection for a patient. When no active treatment plan covers
 * the date or no routine is scheduled for that day, {@code routineId} is null and
 * the counters are zero.
 */
public record DailyScheduleResource(
        UUID patientId,
        LocalDate date,
        UUID routineId,
        Integer totalSeries,
        Integer estimatedDurationMinutes
) {}
