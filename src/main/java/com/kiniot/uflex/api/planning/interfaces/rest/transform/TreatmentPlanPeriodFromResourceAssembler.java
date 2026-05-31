package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanPeriod;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.TreatmentPlanPeriodResource;

import java.time.LocalDate;

public class TreatmentPlanPeriodFromResourceAssembler {
    public static TreatmentPlanPeriod toValueObjectFromResource(TreatmentPlanPeriodResource resource) {
        return new TreatmentPlanPeriod(
                LocalDate.parse(resource.startsAt()),
                LocalDate.parse(resource.endsAt()));
    }
}
