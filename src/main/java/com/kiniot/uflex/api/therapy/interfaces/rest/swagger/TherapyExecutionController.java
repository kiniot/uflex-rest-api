package com.kiniot.uflex.api.therapy.interfaces.rest.swagger;

import com.kiniot.uflex.api.therapy.interfaces.rest.resources.RecordAnomalousMovementResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.RecordValidRepetitionResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.ReportPainLevelResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SerieDetailsResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SerieProgressResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SessionProgressResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(value = "/api/v1/therapy-sessions", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Therapy Execution", description = "Therapy Session Execution Endpoints")
public interface TherapyExecutionController {

    @PatchMapping("/{id}/series/{serieId}/start")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    @Operation(summary = "Start a serie", description = "Transitions the serie to Started and emits SerieStarted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serie started."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Session or serie not found."),
            @ApiResponse(responseCode = "409", description = "Session not in progress."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<SerieDetailsResource> startSerie(@PathVariable UUID id, @PathVariable UUID serieId);

    @PostMapping("/{id}/series/{serieId}/repetitions")
    @Operation(summary = "Record a valid repetition", description = "Persists a validated repetition from the Edge App. Idempotent via X-Edge-Sequence-Id header.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Repetition recorded; returns the updated serie progress."),
            @ApiResponse(responseCode = "400", description = "Invalid input data."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Session or serie not found."),
            @ApiResponse(responseCode = "409", description = "Session not in progress or serie not started."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<SerieProgressResource> recordRepetition(
            @PathVariable UUID id,
            @PathVariable UUID serieId,
            @RequestHeader(value = "X-Edge-Sequence-Id", required = false) UUID edgeSequenceId,
            @Valid @RequestBody RecordValidRepetitionResource resource);

    @PostMapping("/{id}/anomalies")
    @Operation(summary = "Record anomalous movement", description = "Persists an anomaly alert from the Edge App. Emits AnomalousMovementDetected or ExcessiveMovementAlertIssued.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Anomaly recorded."),
            @ApiResponse(responseCode = "400", description = "Invalid input data."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Session not found."),
            @ApiResponse(responseCode = "409", description = "Session not in progress."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<Void> recordAnomalousMovement(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Edge-Sequence-Id", required = false) UUID edgeSequenceId,
            @Valid @RequestBody RecordAnomalousMovementResource resource);

    @PatchMapping("/{id}/pain")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @Operation(summary = "Report pain level", description = "Registers the patient's self-reported pain level (0–10). Emits PainLevelReported.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pain level registered."),
            @ApiResponse(responseCode = "400", description = "Invalid input data."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Session not found."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<Void> reportPainLevel(
            @PathVariable UUID id,
            @Valid @RequestBody ReportPainLevelResource resource);

    @Transactional(readOnly = true)
    @GetMapping("/{id}/progress")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    @Operation(summary = "Get session progress", description = "Returns the live execution state: active serie, repetition counts, and overall status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress retrieved."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Session not found."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<SessionProgressResource> getSessionProgress(@PathVariable UUID id);

    @GetMapping("/{id}/series/{serieId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    @Operation(summary = "Get serie details", description = "Returns the clinical parameters of a serie: angle range, target reps, durations.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serie details retrieved."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Session or serie not found."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<SerieDetailsResource> getSerieDetails(@PathVariable UUID id, @PathVariable UUID serieId);
}
