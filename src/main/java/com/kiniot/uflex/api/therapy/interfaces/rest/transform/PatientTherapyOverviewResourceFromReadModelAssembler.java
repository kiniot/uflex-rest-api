package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.queries.PatientTherapyOverview;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.PatientTherapyOverviewResource;

public final class PatientTherapyOverviewResourceFromReadModelAssembler {

    private PatientTherapyOverviewResourceFromReadModelAssembler() {}

    public static PatientTherapyOverviewResource toResourceFromReadModel(PatientTherapyOverview overview) {
        return PatientTherapyOverviewResource.builder()
                .patientId(overview.patientId())
                .patientFullName(overview.patientFullName())
                .totalSessions(overview.totalSessions())
                .completedSessions(overview.completedSessions())
                .sessionsRequiringReview(overview.sessionsRequiringReview())
                .lastSessionAt(overview.lastSessionAt())
                .totalRepetitions(overview.totalRepetitions())
                .goodRepetitions(overview.goodRepetitions())
                .averageAchievedRom(overview.averageAchievedRom())
                .hasActiveSession(overview.hasActiveSession())
                .build();
    }
}
