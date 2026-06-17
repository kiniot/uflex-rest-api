package com.kiniot.uflex.api.therapy.interfaces.rest.controllers;

import com.kiniot.uflex.api.therapy.domain.model.queries.GetActiveTherapySessionByPatientIdQuery;
import com.kiniot.uflex.api.therapy.domain.model.queries.GetDailyScheduleQuery;
import com.kiniot.uflex.api.therapy.domain.model.queries.GetSessionSummaryQuery;
import com.kiniot.uflex.api.therapy.domain.services.TherapySessionCommandService;
import com.kiniot.uflex.api.therapy.domain.services.TherapySessionQueryService;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.CancelTherapySessionResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.ConfirmHardwareReadinessResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.DailyScheduleResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.InitiateTherapyPreparationResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SessionSummaryResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.TherapySessionResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.DailyScheduleResourceFromDtoAssembler;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.SessionSummaryResourceFromEntityAssembler;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.TherapySessionResourceFromEntityAssembler;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.CancelTherapySessionCommandFromResourceAssembler;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.ConfirmHardwareReadinessCommandFromResourceAssembler;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.InitiateTherapyPreparationCommandFromResourceAssembler;
import com.kiniot.uflex.api.therapy.domain.model.commands.FinalizeTherapySessionCommand;
import com.kiniot.uflex.api.therapy.domain.model.commands.StartTherapySessionCommand;
import com.kiniot.uflex.api.therapy.interfaces.rest.swagger.TherapySessionController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TherapySessionControllerImpl implements TherapySessionController {

    private final TherapySessionCommandService therapySessionCommandService;
    private final TherapySessionQueryService therapySessionQueryService;

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    public ResponseEntity<TherapySessionResource> initiateTherapyPreparation(InitiateTherapyPreparationResource resource) {
        var command = InitiateTherapyPreparationCommandFromResourceAssembler.toCommandFromResource(resource);
        var session = therapySessionCommandService.handle(command);
        var response = TherapySessionResourceFromEntityAssembler.toResponseFromEntity(session);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    public ResponseEntity<TherapySessionResource> confirmHardwareReadiness(UUID id, ConfirmHardwareReadinessResource resource) {
        var command = ConfirmHardwareReadinessCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var session = therapySessionCommandService.handle(command);
        return ResponseEntity.ok(TherapySessionResourceFromEntityAssembler.toResponseFromEntity(session));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    public ResponseEntity<TherapySessionResource> startTherapySession(UUID id) {
        var session = therapySessionCommandService.handle(new StartTherapySessionCommand(id));
        return ResponseEntity.ok(TherapySessionResourceFromEntityAssembler.toResponseFromEntity(session));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    public ResponseEntity<TherapySessionResource> finalizeTherapySession(UUID id) {
        var session = therapySessionCommandService.handle(new FinalizeTherapySessionCommand(id));
        return ResponseEntity.ok(TherapySessionResourceFromEntityAssembler.toResponseFromEntity(session));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    public ResponseEntity<TherapySessionResource> cancelTherapySession(UUID id, CancelTherapySessionResource resource) {
        var command = CancelTherapySessionCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var session = therapySessionCommandService.handle(command);
        return ResponseEntity.ok(TherapySessionResourceFromEntityAssembler.toResponseFromEntity(session));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    public ResponseEntity<TherapySessionResource> getActiveTherapySession(UUID patientId) {
        var session = therapySessionQueryService.handle(new GetActiveTherapySessionByPatientIdQuery(patientId));
        return ResponseEntity.ok(TherapySessionResourceFromEntityAssembler.toResponseFromEntity(session));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    public ResponseEntity<SessionSummaryResource> getSessionSummary(UUID id) {
        var session = therapySessionQueryService.handle(new GetSessionSummaryQuery(id));
        return ResponseEntity.ok(SessionSummaryResourceFromEntityAssembler.toResponseFromEntity(session));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    public ResponseEntity<DailyScheduleResource> getDailySchedule(UUID patientId, LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        var routine = therapySessionQueryService.handle(new GetDailyScheduleQuery(patientId, target));
        return ResponseEntity.ok(DailyScheduleResourceFromDtoAssembler.toResourceFromDto(patientId, target, routine));
    }
}
