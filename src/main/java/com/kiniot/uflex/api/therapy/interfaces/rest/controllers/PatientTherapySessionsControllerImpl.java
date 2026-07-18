package com.kiniot.uflex.api.therapy.interfaces.rest.controllers;

import com.kiniot.uflex.api.therapy.domain.model.queries.GetTherapySessionHistoryQuery;
import com.kiniot.uflex.api.therapy.domain.services.TherapySessionQueryService;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.TherapySessionHistoryItemResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.swagger.PatientTherapySessionsController;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.TherapySessionHistoryItemResourceFromReadModelAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PatientTherapySessionsControllerImpl implements PatientTherapySessionsController {

    private final TherapySessionQueryService therapySessionQueryService;

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    public ResponseEntity<List<TherapySessionHistoryItemResource>> getTherapySessionHistory(
            UUID patientId, UUID treatmentPlanId) {
        var history = therapySessionQueryService.handle(
                new GetTherapySessionHistoryQuery(patientId, treatmentPlanId));
        return ResponseEntity.ok(history.stream()
                .map(TherapySessionHistoryItemResourceFromReadModelAssembler::toResourceFromReadModel)
                .toList());
    }
}
