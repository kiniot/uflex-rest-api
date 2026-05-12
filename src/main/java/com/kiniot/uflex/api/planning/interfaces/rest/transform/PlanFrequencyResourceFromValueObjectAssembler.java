package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanFrequency;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.PlanFrequencyResource;

public class PlanFrequencyResourceFromValueObjectAssembler {
    public static PlanFrequencyResource toResourceFromValueObject(PlanFrequency valueObject) {
        return new PlanFrequencyResource(valueObject.occurrences(), valueObject.unit().name());
    }
}
