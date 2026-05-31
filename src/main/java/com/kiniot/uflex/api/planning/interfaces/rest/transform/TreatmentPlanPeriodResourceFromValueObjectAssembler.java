package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanPeriod;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.TreatmentPlanPeriodResource;

public class TreatmentPlanPeriodResourceFromValueObjectAssembler {
    public static TreatmentPlanPeriodResource toResourceFromValueObject(TreatmentPlanPeriod valueObject) {
        return new TreatmentPlanPeriodResource(
                valueObject.startsAt().toString(),
                valueObject.endsAt().toString());
    }
}
