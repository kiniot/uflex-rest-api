package com.kiniot.uflex.api.planning.domain.services;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.commands.ActivateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CancelTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CompleteTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateRoutineCommand;

import java.util.Optional;

public interface TreatmentPlanCommandService {
    Optional<TreatmentPlan> handle(CreateTreatmentPlanCommand command);
    Optional<TreatmentPlan> handle(UpdateTreatmentPlanCommand command);
    Optional<TreatmentPlan> handle(ActivateTreatmentPlanCommand command);
    Optional<TreatmentPlan> handle(CompleteTreatmentPlanCommand command);
    Optional<TreatmentPlan> handle(CancelTreatmentPlanCommand command);
    void handle(RemoveTreatmentPlanCommand command);
    Optional<TreatmentPlan> handle(CreateRoutineCommand command);
    Optional<TreatmentPlan> handle(UpdateRoutineCommand command);
    Optional<TreatmentPlan> handle(RemoveRoutineCommand command);
}
