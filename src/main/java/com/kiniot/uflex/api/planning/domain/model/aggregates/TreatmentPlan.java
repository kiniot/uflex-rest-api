package com.kiniot.uflex.api.planning.domain.model.aggregates;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateRoutineCommand;
import com.kiniot.uflex.api.planning.domain.exceptions.DuplicateRoutineOrderException;
import com.kiniot.uflex.api.planning.domain.exceptions.DuplicateRoutineScheduleException;
import com.kiniot.uflex.api.planning.domain.exceptions.RoutineWithOrderNotFoundException;
import com.kiniot.uflex.api.planning.domain.model.entities.Routine;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanFrequency;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineSchedule;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class TreatmentPlan extends AuditableAbstractAggregateRoot<TreatmentPlan, TreatmentPlanId> {

    @EmbeddedId
    private TreatmentPlanId id;

    @Embedded
    private PlanName planName;

    @Embedded
    private PlanFrequency frequency;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "treatment_plan_id", nullable = false)
    private List<Routine> routines;

    @Embedded
    private ClinicId clinicId;

    protected TreatmentPlan() {}

    public TreatmentPlan(CreateTreatmentPlanCommand command, ClinicId clinicId) {
        this.id = command.id();
        this.planName = command.name();
        this.frequency = command.frequency();
        this.routines = new ArrayList<>();
        this.clinicId = clinicId;
    }

    public void update(UpdateTreatmentPlanCommand command) {
        this.planName = command.name();
        this.frequency = command.frequency();
    }

    public void addRoutine(CreateRoutineCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Routine command cannot be null");
        }
        Routine routine = new Routine(command);
        validateUniqueRoutineOrder(routine);
        validateUniqueRoutineSchedule(routine);
        this.routines.add(routine);
    }

    public void removeRoutine(RemoveRoutineCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Remove routine command cannot be null");
        }

        boolean removed = this.routines.removeIf(routine -> routine.getOrder().equals(command.order()));
        if (!removed) {
            throw new RoutineWithOrderNotFoundException(command.order().value());
        }
    }

    public void updateRoutine(UpdateRoutineCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Update routine command cannot be null");
        }

        Routine routine = findRoutineByOrder(command.currentOrder());
        validateUniqueRoutineOrder(command.newOrder(), routine);
        validateUniqueRoutineSchedule(command.schedule(), routine);
        routine.update(command);
    }

    private void validateUniqueRoutineOrder(Routine routine) {
        validateUniqueRoutineOrder(routine.getOrder(), null);
    }

    private void validateUniqueRoutineOrder(RoutineOrder order, Routine routineToIgnore) {
        boolean duplicatedOrder = this.routines.stream()
                .filter(existingRoutine -> !existingRoutine.equals(routineToIgnore))
                .anyMatch(existingRoutine -> existingRoutine.getOrder().equals(order));

        if (duplicatedOrder) {
            throw new DuplicateRoutineOrderException(order.value());
        }
    }

    private void validateUniqueRoutineSchedule(Routine routine) {
        validateUniqueRoutineSchedule(routine.getSchedule(), null);
    }

    private void validateUniqueRoutineSchedule(RoutineSchedule schedule, Routine routineToIgnore) {
        boolean duplicatedSchedule = this.routines.stream()
                .filter(existingRoutine -> !existingRoutine.equals(routineToIgnore))
                .anyMatch(existingRoutine -> existingRoutine.getSchedule().equals(schedule));

        if (duplicatedSchedule) {
            throw new DuplicateRoutineScheduleException(schedule.dayOfWeek().name(), schedule.scheduledTime().toString());
        }
    }

    private Routine findRoutineByOrder(RoutineOrder order) {
        return this.routines.stream()
                .filter(routine -> routine.getOrder().equals(order))
                .findFirst()
                .orElseThrow(() -> new RoutineWithOrderNotFoundException(order.value()));
    }

    @Override
    public TreatmentPlanId getId() {
        return id;
    }
}
