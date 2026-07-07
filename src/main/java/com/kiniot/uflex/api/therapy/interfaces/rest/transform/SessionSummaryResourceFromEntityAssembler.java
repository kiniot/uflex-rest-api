package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.entities.CompletedRepetition;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.RepetitionClassification;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieStatus;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SessionSummaryResource;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

public final class SessionSummaryResourceFromEntityAssembler {

    private SessionSummaryResourceFromEntityAssembler() {}

    public static SessionSummaryResource toResponseFromEntity(TherapySession session) {
        List<Serie> series = session.getRoutine() != null ? session.getRoutine().getSeries() : List.of();
        int totalSeries = series.size();
        int completedSeries = (int) series.stream()
                .filter(s -> s.getStatus() == SerieStatus.Completed)
                .count();

        List<CompletedRepetition> repetitions = series.stream()
                .flatMap(s -> s.getRepetitions().stream())
                .toList();
        int totalRepetitions = repetitions.size();
        long good = repetitions.stream()
                .filter(r -> r.getClassification() == RepetitionClassification.Good).count();
        long incomplete = repetitions.stream()
                .filter(r -> r.getClassification() == RepetitionClassification.Incomplete).count();
        long unsafe = repetitions.stream()
                .filter(r -> r.getClassification() == RepetitionClassification.Unsafe).count();

        OptionalDouble avgRom = repetitions.stream()
                .map(CompletedRepetition::getAchievedRom)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();
        Double averageAchievedRom = avgRom.isPresent() ? avgRom.getAsDouble() : null;

        return SessionSummaryResource.builder()
                .sessionId(session.getId() != null ? session.getId().id() : null)
                .patientId(session.getPatientId() != null ? session.getPatientId().id() : null)
                .totalSeries(totalSeries)
                .completedSeries(completedSeries)
                .totalRepetitions(totalRepetitions)
                .goodRepetitions((int) good)
                .incompleteRepetitions((int) incomplete)
                .unsafeRepetitions((int) unsafe)
                .averageAchievedRom(averageAchievedRom)
                .painLevel(session.getPainLevel() != null ? session.getPainLevel().value() : null)
                .painReportsCount(session.getPainReportsCount())
                .highPainReportsCount(session.getHighPainReportsCount())
                .maxReportedPainLevel(session.getMaxReportedPainLevel())
                .requiresClinicalReview(session.getRequiresClinicalReview())
                .compensatoryMovementsDetected(session.getCompensatoryMovementsCount())
                .startedAt(session.getStartedAt())
                .finalizedAt(session.getFinalizedAt())
                .build();
    }
}
