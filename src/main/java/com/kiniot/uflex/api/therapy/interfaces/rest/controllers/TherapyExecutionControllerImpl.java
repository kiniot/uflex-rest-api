package com.kiniot.uflex.api.therapy.interfaces.rest.controllers;

import com.kiniot.uflex.api.therapy.domain.model.commands.StartSerieCommand;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.queries.GetSerieDetailsQuery;
import com.kiniot.uflex.api.therapy.domain.model.queries.GetSessionProgressQuery;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieStatus;
import com.kiniot.uflex.api.therapy.domain.services.TherapySessionCommandService;
import com.kiniot.uflex.api.therapy.domain.services.TherapySessionQueryService;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.RecordCompensatoryMovementResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.RecordValidRepetitionResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.ReportPainLevelResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SerieDetailsResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SerieProgressResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SessionProgressResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.SerieDetailsResourceFromEntityAssembler;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.SessionProgressResourceFromEntityAssembler;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.RecordCompensatoryMovementCommandFromResourceAssembler;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.RecordValidRepetitionCommandFromResourceAssembler;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.ReportPainLevelCommandFromResourceAssembler;
import com.kiniot.uflex.api.therapy.interfaces.rest.swagger.TherapyExecutionController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TherapyExecutionControllerImpl implements TherapyExecutionController {

    private final TherapySessionCommandService therapySessionCommandService;
    private final TherapySessionQueryService therapySessionQueryService;

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    public ResponseEntity<SerieDetailsResource> startSerie(UUID id, UUID serieId) {
        var session = therapySessionCommandService.handle(new StartSerieCommand(id, serieId));
        Serie serie = session.findSerie(SerieId.of(serieId))
                .orElseThrow(() -> new IllegalStateException("Serie not found after startSerie: " + serieId));
        return ResponseEntity.ok(SerieDetailsResourceFromEntityAssembler.toResponseFromEntity(serie));
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_EDGE')")
    public ResponseEntity<SerieProgressResource> recordRepetition(
            UUID id, UUID serieId, UUID edgeSequenceId, RecordValidRepetitionResource resource) {
        var command = RecordValidRepetitionCommandFromResourceAssembler
                .toCommandFromResource(id, serieId, edgeSequenceId, resource);
        var session = therapySessionCommandService.handle(command);
        Serie serie = session.findSerie(SerieId.of(serieId))
                .orElseThrow(() -> new IllegalStateException("Serie not found after recordRepetition: " + serieId));
        var response = SerieProgressResource.builder()
                .serieId(serie.getId().id())
                .exerciseId(serie.getExerciseId() != null ? serie.getExerciseId().id() : null)
                .currentRepetitions(serie.getCurrentRepetitions())
                .targetRepetitions(serie.getTargetRepetitions())
                .status(SerieStatus.toStringOrNull(serie.getStatus()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_EDGE')")
    public ResponseEntity<Void> recordCompensatoryMovement(UUID id, UUID edgeSequenceId, RecordCompensatoryMovementResource resource) {
        var command = RecordCompensatoryMovementCommandFromResourceAssembler
                .toCommandFromResource(id, edgeSequenceId, resource);
        therapySessionCommandService.handle(command);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .replacePath("/api/v1/therapy-sessions/{id}/progress")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<Void> reportPainLevel(UUID id, ReportPainLevelResource resource) {
        var command = ReportPainLevelCommandFromResourceAssembler.toCommandFromResource(id, resource);
        therapySessionCommandService.handle(command);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    public ResponseEntity<SessionProgressResource> getSessionProgress(UUID id) {
        var session = therapySessionQueryService.handle(new GetSessionProgressQuery(id));
        return ResponseEntity.ok(SessionProgressResourceFromEntityAssembler.toResponseFromEntity(session));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    public ResponseEntity<SerieDetailsResource> getSerieDetails(UUID id, UUID serieId) {
        var serie = therapySessionQueryService.handle(new GetSerieDetailsQuery(id, serieId));
        return ResponseEntity.ok(SerieDetailsResourceFromEntityAssembler.toResponseFromEntity(serie));
    }
}
