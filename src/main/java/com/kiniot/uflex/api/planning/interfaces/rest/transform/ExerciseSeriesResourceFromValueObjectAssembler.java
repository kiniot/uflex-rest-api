package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseSeries;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.ExerciseSeriesResource;

public class ExerciseSeriesResourceFromValueObjectAssembler {
    public static ExerciseSeriesResource toResourceFromValueObject(ExerciseSeries valueObject) {
        return new ExerciseSeriesResource(
                valueObject.order().value(),
                valueObject.exerciseId().id().toString(),
                valueObject.rangeOfMotion().degrees(),
                valueObject.repetitions().value(),
                valueObject.duration().seconds(),
                valueObject.restDuration().seconds());
    }
}
