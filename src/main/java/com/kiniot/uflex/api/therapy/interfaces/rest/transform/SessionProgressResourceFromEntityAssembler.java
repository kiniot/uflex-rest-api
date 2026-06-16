package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieStatus;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SerieProgressResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SessionProgressResource;

import java.util.List;
import java.util.UUID;

public final class SessionProgressResourceFromEntityAssembler {

    private SessionProgressResourceFromEntityAssembler() {}

    public static SessionProgressResource toResponseFromEntity(TherapySession session) {
        List<Serie> series = session.getRoutine() != null ? session.getRoutine().getSeries() : List.of();

        UUID currentSerieId = series.stream()
                .filter(s -> s.getStatus() == SerieStatus.Started)
                .map(s -> s.getId().id())
                .findFirst()
                .orElse(null);

        List<SerieProgressResource> seriesProgress = series.stream()
                .map(s -> SerieProgressResource.builder()
                        .serieId(s.getId().id())
                        .exerciseId(s.getExerciseId() != null ? s.getExerciseId().id() : null)
                        .currentRepetitions(s.getCurrentRepetitions())
                        .targetRepetitions(s.getTargetRepetitions())
                        .status(SerieStatus.toStringOrNull(s.getStatus()))
                        .build())
                .toList();

        return SessionProgressResource.builder()
                .sessionId(session.getId() != null ? session.getId().id() : null)
                .status(SessionStatus.toStringOrNull(session.getStatus()))
                .currentSerieId(currentSerieId)
                .painLevel(session.getPainLevel() != null ? session.getPainLevel().value() : null)
                .requiresClinicalReview(session.isRequiresClinicalReview())
                .seriesProgress(seriesProgress)
                .build();
    }
}
