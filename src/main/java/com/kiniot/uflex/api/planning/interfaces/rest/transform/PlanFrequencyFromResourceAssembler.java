package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.FrequencyUnit;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanFrequency;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.PlanFrequencyResource;

public class PlanFrequencyFromResourceAssembler {
    public static PlanFrequency toValueObjectFromResource(PlanFrequencyResource resource) {
        return new PlanFrequency(resource.occurrences(), FrequencyUnit.valueOf(resource.unit()));
    }
}
