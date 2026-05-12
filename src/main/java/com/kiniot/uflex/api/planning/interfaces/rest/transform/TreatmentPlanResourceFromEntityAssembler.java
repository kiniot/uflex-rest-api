package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.TreatmentPlanResource;

public class TreatmentPlanResourceFromEntityAssembler {
    public static TreatmentPlanResource toResourceFromEntity(TreatmentPlan entity) {
        var treatmentPlanId = entity.getId() != null ? entity.getId().id().toString() : null;
        var clinicId = entity.getClinicId() != null && entity.getClinicId().clinicId() != null
                ? entity.getClinicId().clinicId().toString()
                : null;
        var routines = entity.getRoutines() != null
                ? entity.getRoutines().stream().map(RoutineResourceFromEntityAssembler::toResourceFromEntity).toList()
                : java.util.List.of();

        return new TreatmentPlanResource(
                treatmentPlanId,
                entity.getPlanName().name(),
                PlanFrequencyResourceFromValueObjectAssembler.toResourceFromValueObject(entity.getFrequency()),
                clinicId,
                routines);
    }
}
