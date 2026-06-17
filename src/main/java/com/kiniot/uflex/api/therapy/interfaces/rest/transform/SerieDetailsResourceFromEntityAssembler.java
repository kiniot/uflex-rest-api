package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieStatus;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SerieDetailsResource;

public final class SerieDetailsResourceFromEntityAssembler {

    private SerieDetailsResourceFromEntityAssembler() {}

    public static SerieDetailsResource toResponseFromEntity(Serie serie) {
        return SerieDetailsResource.builder()
                .serieId(serie.getId() != null ? serie.getId().id() : null)
                .exerciseId(serie.getExerciseId() != null ? serie.getExerciseId().id() : null)
                .targetRepetitions(serie.getTargetRepetitions())
                .minAngle(serie.getAngleThreshold() != null ? serie.getAngleThreshold().minAngle() : null)
                .maxAngle(serie.getAngleThreshold() != null ? serie.getAngleThreshold().maxAngle() : null)
                .durationSeconds(serie.getDurationSeconds())
                .restDurationSeconds(serie.getRestDurationSeconds())
                .status(SerieStatus.toStringOrNull(serie.getStatus()))
                .build();
    }
}
