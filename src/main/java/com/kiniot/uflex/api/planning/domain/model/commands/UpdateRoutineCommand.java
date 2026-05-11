package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseSeries;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineSchedule;

import java.util.List;

public record UpdateRoutineCommand(
        RoutineOrder currentOrder,
        RoutineName name,
        RoutineOrder newOrder,
        RoutineSchedule schedule,
        List<ExerciseSeries> exerciseSeries
) {
    public UpdateRoutineCommand {
        if (currentOrder == null) {
            throw new IllegalArgumentException("Current routine order cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Routine name cannot be null");
        }
        if (newOrder == null) {
            throw new IllegalArgumentException("New routine order cannot be null");
        }
        if (schedule == null) {
            throw new IllegalArgumentException("Routine schedule cannot be null");
        }
        if (exerciseSeries == null || exerciseSeries.isEmpty()) {
            throw new IllegalArgumentException("Routine must contain at least one exercise series");
        }
    }
}
