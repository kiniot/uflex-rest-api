package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.entities.CompletedRepetition;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.KitSerial;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.RepetitionClassification;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieStatus;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SerieExecutionResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.TherapySessionDetailResource;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Maps a session whose {@code routine.series} and their repetitions have already been hydrated by
 * the query service. Walking a non-hydrated session here would throw, since open-in-view is off.
 */
public final class TherapySessionDetailResourceFromEntityAssembler {

    private TherapySessionDetailResourceFromEntityAssembler() {}

    public static TherapySessionDetailResource toResourceFromEntity(TherapySession session) {
        List<Serie> series = session.getRoutine() != null ? session.getRoutine().getSeries() : List.of();

        List<CompletedRepetition> repetitions = series.stream()
                .flatMap(serie -> serie.getRepetitions().stream())
                .toList();

        OptionalDouble avgRom = repetitions.stream()
                .map(CompletedRepetition::getAchievedRom)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();

        return TherapySessionDetailResource.builder()
                .sessionId(session.getId() != null ? session.getId().id() : null)
                .patientId(session.getPatientId() != null ? session.getPatientId().id() : null)
                .treatmentPlanId(session.getTreatmentPlanId() != null ? session.getTreatmentPlanId().id() : null)
                .planningRoutineId(session.getRoutine() != null ? session.getRoutine().getPlanningRoutineId() : null)
                .iotDeviceId(KitSerial.toStringOrNull(session.getIotDeviceId()))
                .status(SessionStatus.toStringOrNull(session.getStatus()))
                .sensorsPlaced(session.getSensorsPlaced())
                .startedAt(session.getStartedAt())
                .finalizedAt(session.getFinalizedAt())
                .cancellationReason(session.getCancellationReason())
                .totalSeries(series.size())
                .completedSeries((int) series.stream()
                        .filter(serie -> serie.getStatus() == SerieStatus.Completed)
                        .count())
                .totalRepetitions(repetitions.size())
                .goodRepetitions(countByClassification(repetitions, RepetitionClassification.Good))
                .incompleteRepetitions(countByClassification(repetitions, RepetitionClassification.Incomplete))
                .unsafeRepetitions(countByClassification(repetitions, RepetitionClassification.Unsafe))
                .averageAchievedRom(avgRom.isPresent() ? avgRom.getAsDouble() : null)
                .painLevel(session.getPainLevel() != null ? session.getPainLevel().value() : null)
                .painReportsCount(session.getPainReportsCount())
                .highPainReportsCount(session.getHighPainReportsCount())
                .maxReportedPainLevel(session.getMaxReportedPainLevel())
                .requiresClinicalReview(session.getRequiresClinicalReview())
                .compensatoryMovementsDetected(session.getCompensatoryMovementsCount())
                .series(series.stream()
                        .map(TherapySessionDetailResourceFromEntityAssembler::toSerieExecutionResource)
                        .toList())
                .compensatoryMovements(session.getCompensatoryMovements().stream()
                        .sorted(Comparator.comparing(movement -> movement.getDetectedAt(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(CompensatoryMovementResourceFromEntityAssembler::toResourceFromEntity)
                        .toList())
                .build();
    }

    private static int countByClassification(List<CompletedRepetition> repetitions,
                                             RepetitionClassification classification) {
        return (int) repetitions.stream()
                .filter(repetition -> repetition.getClassification() == classification)
                .count();
    }

    private static SerieExecutionResource toSerieExecutionResource(Serie serie) {
        return SerieExecutionResource.builder()
                .serieId(serie.getId() != null ? serie.getId().id() : null)
                .exerciseId(serie.getExerciseId() != null ? serie.getExerciseId().id() : null)
                .targetRepetitions(serie.getTargetRepetitions())
                .targetRom(serie.getTargetRom())
                .movementType(serie.getMovementType())
                .bodyPart(serie.getBodyPart())
                .durationSeconds(serie.getDurationSeconds())
                .restDurationSeconds(serie.getRestDurationSeconds())
                .status(SerieStatus.toStringOrNull(serie.getStatus()))
                .repetitions(serie.getRepetitions().stream()
                        .sorted(Comparator.comparing(CompletedRepetition::getRecordedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(CompletedRepetitionResourceFromEntityAssembler::toResourceFromEntity)
                        .toList())
                .build();
    }
}
