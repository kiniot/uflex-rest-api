package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.RoutineResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.TreatmentPlanResource;

import java.util.List;

public class TreatmentPlanResourceFromEntityAssembler {
    public static TreatmentPlanResource toResourceFromEntity(TreatmentPlan entity) {
        var treatmentPlanId = entity.getId() != null ? entity.getId().id().toString() : null;
        var planName = entity.getPlanName() != null ? entity.getPlanName().name() : null;
        var clinicId = entity.getClinicId() != null && entity.getClinicId().id() != null
                ? entity.getClinicId().id().toString()
                : null;
        var frequency = entity.getFrequency() != null
                ? PlanFrequencyResourceFromValueObjectAssembler.toResourceFromValueObject(entity.getFrequency())
                : null;
        List<RoutineResource> routines = entity.getRoutines() != null
                ? entity.getRoutines().stream().map(RoutineResourceFromEntityAssembler::toResourceFromEntity).toList()
                : List.of();

        return new TreatmentPlanResource(
                treatmentPlanId,
                planName,
                frequency,
                clinicId,
                routines);
    }
}
