package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseSeries;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineSchedule;

import java.util.List;

public record CreateTreatmentPlanRoutineCommand(
        RoutineName name,
        RoutineOrder order,
        RoutineSchedule schedule,
        List<ExerciseSeries> exerciseSeries
) {
    public CreateTreatmentPlanRoutineCommand {
        if (name == null) {
            throw new IllegalArgumentException("Routine name cannot be null");
        }
        if (order == null) {
            throw new IllegalArgumentException("Routine order cannot be null");
        }
        if (schedule == null) {
            throw new IllegalArgumentException("Routine schedule cannot be null");
        }
        if (exerciseSeries == null || exerciseSeries.isEmpty()) {
            throw new IllegalArgumentException("Routine must contain at least one exercise series");
        }
    }
}
