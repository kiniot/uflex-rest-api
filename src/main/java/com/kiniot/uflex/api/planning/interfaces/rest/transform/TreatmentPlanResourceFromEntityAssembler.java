package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.RoutineResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.TreatmentPlanResource;

import java.util.List;

public class TreatmentPlanResourceFromEntityAssembler {
    public static TreatmentPlanResource toResourceFromEntity(TreatmentPlan entity) {
        var treatmentPlanId = entity.getId() != null ? entity.getId().id().toString() : null;
        var patientId = entity.getPatientId() != null ? entity.getPatientId().patientId().toString() : null;
        var planName = entity.getPlanName() != null ? entity.getPlanName().name() : null;
        var status = entity.getStatus() != null ? entity.getStatus().name() : null;
        var period = entity.getPeriod() != null
                ? TreatmentPlanPeriodResourceFromValueObjectAssembler.toResourceFromValueObject(entity.getPeriod())
                : null;
        List<RoutineResource> routines = entity.getRoutines() != null
                ? entity.getRoutines().stream().map(RoutineResourceFromEntityAssembler::toResourceFromEntity).toList()
                : List.of();

        return new TreatmentPlanResource(
                treatmentPlanId,
                patientId,
                planName,
                status,
                period,
                routines);
    }
}
