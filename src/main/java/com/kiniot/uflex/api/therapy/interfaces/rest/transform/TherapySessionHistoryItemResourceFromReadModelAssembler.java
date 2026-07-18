package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.queries.TherapySessionHistoryItem;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.TherapySessionHistoryItemResource;

public final class TherapySessionHistoryItemResourceFromReadModelAssembler {

    private TherapySessionHistoryItemResourceFromReadModelAssembler() {}

    public static TherapySessionHistoryItemResource toResourceFromReadModel(TherapySessionHistoryItem item) {
        return TherapySessionHistoryItemResource.builder()
                .sessionId(item.sessionId())
                .status(SessionStatus.toStringOrNull(item.status()))
                .startedAt(item.startedAt())
                .finalizedAt(item.finalizedAt())
                .treatmentPlanId(item.treatmentPlanId())
                .planningRoutineId(item.planningRoutineId())
                .totalSeries(item.totalSeries())
                .completedSeries(item.completedSeries())
                .totalRepetitions(item.totalRepetitions())
                .goodRepetitions(item.goodRepetitions())
                .incompleteRepetitions(item.incompleteRepetitions())
                .unsafeRepetitions(item.unsafeRepetitions())
                .averageAchievedRom(item.averageAchievedRom())
                .painLevel(item.painLevel())
                .maxReportedPainLevel(item.maxReportedPainLevel())
                .requiresClinicalReview(item.requiresClinicalReview())
                .compensatoryMovementsDetected(item.compensatoryMovementsDetected())
                .build();
    }
}
