package com.kiniot.uflex.api.planning.domain.exceptions;

public class DuplicateRoutineScheduleException extends RuntimeException {
    public DuplicateRoutineScheduleException(String dayOfWeek, String scheduledTime) {
        super("Routine schedule %s %s already exists within the treatment plan".formatted(dayOfWeek, scheduledTime));
    }
}
