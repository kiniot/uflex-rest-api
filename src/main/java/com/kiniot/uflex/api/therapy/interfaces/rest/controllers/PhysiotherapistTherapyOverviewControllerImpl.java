package com.kiniot.uflex.api.therapy.interfaces.rest.controllers;

import com.kiniot.uflex.api.therapy.domain.model.queries.GetPatientTherapyOverviewQuery;
import com.kiniot.uflex.api.therapy.domain.services.TherapySessionQueryService;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.PatientTherapyOverviewResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.swagger.PhysiotherapistTherapyOverviewController;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.PatientTherapyOverviewResourceFromReadModelAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PhysiotherapistTherapyOverviewControllerImpl implements PhysiotherapistTherapyOverviewController {

    private final TherapySessionQueryService therapySessionQueryService;

    @Override
    @PreAuthorize("hasAuthority('ROLE_PHYSIOTHERAPIST')")
    public ResponseEntity<List<PatientTherapyOverviewResource>> getPatientTherapyOverview() {
        var overview = therapySessionQueryService.handle(new GetPatientTherapyOverviewQuery());
        return ResponseEntity.ok(overview.stream()
                .map(PatientTherapyOverviewResourceFromReadModelAssembler::toResourceFromReadModel)
                .toList());
    }
}
