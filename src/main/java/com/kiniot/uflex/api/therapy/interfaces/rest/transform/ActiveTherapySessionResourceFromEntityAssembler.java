package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.KitSerial;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.ActiveTherapySessionResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SerieDetailsResource;

import java.util.List;

public final class ActiveTherapySessionResourceFromEntityAssembler {

    private ActiveTherapySessionResourceFromEntityAssembler() {}

    public static ActiveTherapySessionResource toResponseFromEntity(TherapySession session) {
        List<Serie> series = session.getRoutine() != null ? session.getRoutine().getSeries() : List.of();
        List<SerieDetailsResource> serieResources = series.stream()
                .map(SerieDetailsResourceFromEntityAssembler::toResponseFromEntity)
                .toList();

        return ActiveTherapySessionResource.builder()
                .id(session.getId() != null ? session.getId().id() : null)
                .patientId(session.getPatientId() != null ? session.getPatientId().id() : null)
                .treatmentPlanId(session.getTreatmentPlanId() != null ? session.getTreatmentPlanId().id() : null)
                .iotDeviceId(KitSerial.toStringOrNull(session.getIotDeviceId()))
                .sensorsPlaced(session.getSensorsPlaced())
                .status(SessionStatus.toStringOrNull(session.getStatus()))
                .painLevel(session.getPainLevel() != null ? session.getPainLevel().value() : null)
                .requiresClinicalReview(session.getRequiresClinicalReview())
                .startedAt(session.getStartedAt())
                .finalizedAt(session.getFinalizedAt())
                .series(serieResources)
                .build();
    }
}
