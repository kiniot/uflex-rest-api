package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.TherapySessionResource;

public final class TherapySessionResourceFromEntityAssembler {

    private TherapySessionResourceFromEntityAssembler() {}

    public static TherapySessionResource toResponseFromEntity(TherapySession session) {
        return TherapySessionResource.builder()
                .id(session.getId() != null ? session.getId().id() : null)
                .patientId(session.getPatientId() != null ? session.getPatientId().id() : null)
                .treatmentPlanId(session.getTreatmentPlanId() != null ? session.getTreatmentPlanId().id() : null)
                .iotDeviceId(session.getIotDeviceId())
                .status(SessionStatus.toStringOrNull(session.getStatus()))
                .painLevel(session.getPainLevel() != null ? session.getPainLevel().value() : null)
                .requiresClinicalReview(session.isRequiresClinicalReview())
                .startedAt(session.getStartedAt())
                .finalizedAt(session.getFinalizedAt())
                .build();
    }
}
