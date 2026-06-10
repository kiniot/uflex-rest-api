package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieStatus;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SessionSummaryResource;

import java.util.List;

public final class SessionSummaryResourceFromEntityAssembler {

    private SessionSummaryResourceFromEntityAssembler() {}

    public static SessionSummaryResource toResponseFromEntity(TherapySession session) {
        List<Serie> series = session.getRoutine() != null ? session.getRoutine().getSeries() : List.of();
        int totalSeries = series.size();
        int completedSeries = (int) series.stream()
                .filter(s -> s.getStatus() == SerieStatus.Validated)
                .count();

        return SessionSummaryResource.builder()
                .sessionId(session.getId() != null ? session.getId().id() : null)
                .patientId(session.getPatientId() != null ? session.getPatientId().id() : null)
                .totalSeries(totalSeries)
                .completedSeries(completedSeries)
                .painLevel(session.getPainLevel() != null ? session.getPainLevel().value() : null)
                .anomaliesDetected(session.getAnomaliesCount())
                .startedAt(session.getStartedAt())
                .finalizedAt(session.getFinalizedAt())
                .build();
    }
}
