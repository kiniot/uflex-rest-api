package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Embeddable
public record RoutineSchedule(
        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 10)
        DayOfWeek dayOfWeek,

        @Column(nullable = false)
        LocalTime scheduledTime
) {
    public RoutineSchedule {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("Day of week cannot be null");
        }
        if (scheduledTime == null) {
            throw new IllegalArgumentException("Scheduled time cannot be null");
        }
    }
}
