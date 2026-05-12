package com.kiniot.uflex.api.planning.domain.services;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.queries.GetAllTreatmentPlansQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlanByIdQuery;

import java.util.List;
import java.util.Optional;

public interface TreatmentPlanQueryService {
    Optional<TreatmentPlan> handle(GetTreatmentPlanByIdQuery query);
    List<TreatmentPlan> handle(GetAllTreatmentPlansQuery query);
}
