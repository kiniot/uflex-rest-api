package com.kiniot.uflex.api.planning.interfaces.acl.dto;

/**
 * Published-language snapshot of the routine a patient is scheduled to perform
 * on a given date. Consumed by the therapy context to build its daily schedule.
 */
public record DailyRoutineDto(
        String routineId,
        int totalSeries,
        int estimatedDurationMinutes
) {}
