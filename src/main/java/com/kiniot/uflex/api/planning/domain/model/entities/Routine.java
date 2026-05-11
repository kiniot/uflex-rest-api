package com.kiniot.uflex.api.planning.domain.model.entities;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseSeries;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineSchedule;
import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
public class Routine extends AuditableModel<RoutineId> {

    @EmbeddedId
    private RoutineId id;

    @Embedded
    private RoutineName name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "routine_order", nullable = false))
    private RoutineOrder order;

    @Embedded
    private RoutineSchedule schedule;

    @ElementCollection
    @CollectionTable(name = "routine_exercise_series", joinColumns = @JoinColumn(name = "routine_id"))
    private List<ExerciseSeries> exerciseSeries;

    protected Routine() {}

    public Routine(CreateRoutineCommand command) {
        this.id = new RoutineId();
        this.name = command.name();
        this.order = command.order();
        this.schedule = command.schedule();
        this.exerciseSeries = new ArrayList<>(command.exerciseSeries());
        validateUniqueExerciseSeriesOrder(this.exerciseSeries);
    }

    public void update(UpdateRoutineCommand command) {
        this.name = command.name();
        this.order = command.newOrder();
        this.schedule = command.schedule();
        this.exerciseSeries = new ArrayList<>(command.exerciseSeries());
        validateUniqueExerciseSeriesOrder(this.exerciseSeries);
    }

    @Override
    public RoutineId getId() {
        return id;
    }

    private void validateUniqueExerciseSeriesOrder(List<ExerciseSeries> exerciseSeries) {
        Set<Integer> usedOrders = new HashSet<>();
        boolean duplicatedOrder = exerciseSeries.stream()
                .map(series -> series.order().value())
                .anyMatch(orderValue -> !usedOrders.add(orderValue));

        if (duplicatedOrder) {
            throw new IllegalArgumentException("Exercise series order must be unique within the routine");
        }
    }
}
