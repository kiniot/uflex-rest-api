package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDuration;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseSeries;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseSeriesOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RangeOfMotion;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RepetitionCount;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RestDuration;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.ExerciseSeriesRequestResource;

import java.util.List;
import java.util.UUID;

public class ExerciseSeriesFromResourceAssembler {
    public static ExerciseSeries toValueObjectFromResource(ExerciseSeriesRequestResource resource) {
        return new ExerciseSeries(
                new ExerciseSeriesOrder(resource.order()),
                new ExerciseId(UUID.fromString(resource.exerciseId())),
                new RangeOfMotion(resource.rangeOfMotionDegrees()),
                new RepetitionCount(resource.repetitions()),
                new ExerciseDuration(resource.durationSeconds()),
                new RestDuration(resource.restDurationSeconds()));
    }

    public static List<ExerciseSeries> toValueObjectListFromResource(List<ExerciseSeriesRequestResource> resources) {
        return resources != null ? resources.stream().map(ExerciseSeriesFromResourceAssembler::toValueObjectFromResource).toList() : List.of();
    }
}
