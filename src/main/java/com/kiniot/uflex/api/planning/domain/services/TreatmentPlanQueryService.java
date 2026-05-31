package com.kiniot.uflex.api.planning.domain.services;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.queries.GetActiveTreatmentPlanByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetAllTreatmentPlansQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetNextScheduledTreatmentPlanByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetScheduledTreatmentPlansByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlanByIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlanByPatientIdAndTreatmentPlanIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlansByPatientIdQuery;

import java.util.List;
import java.util.Optional;

public interface TreatmentPlanQueryService {
    Optional<TreatmentPlan> handle(GetTreatmentPlanByIdQuery query);
    List<TreatmentPlan> handle(GetAllTreatmentPlansQuery query);
    List<TreatmentPlan> handle(GetTreatmentPlansByPatientIdQuery query);
    Optional<TreatmentPlan> handle(GetActiveTreatmentPlanByPatientIdQuery query);
    List<TreatmentPlan> handle(GetScheduledTreatmentPlansByPatientIdQuery query);
    Optional<TreatmentPlan> handle(GetTreatmentPlanByPatientIdAndTreatmentPlanIdQuery query);
    Optional<TreatmentPlan> handle(GetNextScheduledTreatmentPlanByPatientIdQuery query);
}
