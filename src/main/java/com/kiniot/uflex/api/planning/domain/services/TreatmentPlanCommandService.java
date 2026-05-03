package com.kiniot.uflex.api.planning.domain.services;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanCommand;

import java.util.Optional;

public interface TreatmentPlanCommandService {
    Optional<TreatmentPlan> handle(CreateTreatmentPlanCommand command);
}
