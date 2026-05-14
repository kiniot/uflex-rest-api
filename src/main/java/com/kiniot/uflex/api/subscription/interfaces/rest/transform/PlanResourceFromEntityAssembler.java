package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.PlanResource;

public class PlanResourceFromEntityAssembler {
    public static PlanResource toResourceFromEntity(SubscriptionPlan entity) {
        return new PlanResource(
                entity.getId().id(),
                entity.getName(),
                entity.getCode(),
                entity.getMonthlyPrice().amount(),
                entity.getYearlyPrice().amount(),
                entity.getCurrency(),
                entity.getMaxPatients(),
                entity.getMaxPhysiotherapists(),
                entity.getFeatures(),
                entity.isActive()
        );
    }
}
