package com.kiniot.uflex.api.planning.domain.model.aggregates;

import com.kiniot.uflex.api.planning.domain.exceptions.InvalidTreatmentPlanStatusTransitionException;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDuration;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseSeries;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseSeriesOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RangeOfMotion;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RepetitionCount;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RestDuration;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineSchedule;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanPeriod;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanStatus;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TreatmentPlanTests {

    @Test
    void activateChangesScheduledPlanToActive() {
        var treatmentPlan = createTreatmentPlan(TreatmentPlanStatus.SCHEDULED);

        treatmentPlan.activate();

        assertEquals(TreatmentPlanStatus.ACTIVE, treatmentPlan.getStatus());
    }

    @Test
    void completeChangesActivePlanToCompleted() {
        var treatmentPlan = createTreatmentPlan(TreatmentPlanStatus.ACTIVE);

        treatmentPlan.complete();

        assertEquals(TreatmentPlanStatus.COMPLETED, treatmentPlan.getStatus());
    }

    @Test
    void cancelChangesScheduledPlanToCanceled() {
        var treatmentPlan = createTreatmentPlan(TreatmentPlanStatus.SCHEDULED);

        treatmentPlan.cancel();

        assertEquals(TreatmentPlanStatus.CANCELED, treatmentPlan.getStatus());
    }

    @Test
    void activateRejectedForCompletedPlan() {
        var treatmentPlan = createTreatmentPlan(TreatmentPlanStatus.COMPLETED);

        assertThrows(InvalidTreatmentPlanStatusTransitionException.class, treatmentPlan::activate);
    }

    @Test
    void cancelRejectedForCompletedPlan() {
        var treatmentPlan = createTreatmentPlan(TreatmentPlanStatus.COMPLETED);

        assertThrows(InvalidTreatmentPlanStatusTransitionException.class, treatmentPlan::cancel);
    }

    private TreatmentPlan createTreatmentPlan(TreatmentPlanStatus status) {
        var command = new CreateTreatmentPlanCommand(
                new PatientId(),
                new PlanName("Forearm mobility plan"),
                status,
                new TreatmentPlanPeriod(LocalDate.now(), LocalDate.now().plusDays(7)),
                List.of(new CreateTreatmentPlanRoutineCommand(
                        new RoutineName("Morning mobility"),
                        new RoutineOrder(1),
                        new RoutineSchedule(DayOfWeek.MONDAY, LocalTime.of(8, 0)),
                        List.of(new ExerciseSeries(
                                new ExerciseSeriesOrder(1),
                                new ExerciseId(),
                                new RangeOfMotion(60),
                                new RepetitionCount(12),
                                new ExerciseDuration(45),
                                new RestDuration(20)
                        ))
                ))
        );

        return new TreatmentPlan(command, new ClinicId());
    }
}
