package com.kiniot.uflex.api.therapy.interfaces.rest.swagger;

import com.kiniot.uflex.api.therapy.interfaces.rest.resources.CancelTherapySessionResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.ConfirmHardwareReadinessResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.DailyScheduleResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.InitiateTherapyPreparationResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.SessionSummaryResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.TherapySessionResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(value = "/api/v1/therapy-sessions", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Therapy Sessions", description = "Therapy Session Lifecycle Endpoints")
public interface TherapySessionController {

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Initiate therapy preparation", description = "Creates a new TherapySession in Pending state and emits TherapyPreparationInitiated.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session created successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid input data."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "409", description = "Patient already has an active session."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<TherapySessionResource> initiateTherapyPreparation(
            @Valid @RequestBody InitiateTherapyPreparationResource resource);

    @PatchMapping("/{id}/hardware")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Confirm hardware readiness", description = "Registers the IoT sensor snapshot. Transitions Pending → Ready.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hardware readiness confirmed."),
            @ApiResponse(responseCode = "400", description = "Invalid input data."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Session not found."),
            @ApiResponse(responseCode = "409", description = "Sensors not placed or session cannot transition."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<TherapySessionResource> confirmHardwareReadiness(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmHardwareReadinessResource resource);

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Start therapy session", description = "Transitions Ready → InProgress and emits RoutineStarted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session started."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Session not found."),
            @ApiResponse(responseCode = "409", description = "Hardware not confirmed."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<TherapySessionResource> startTherapySession(@PathVariable UUID id);

    @PatchMapping("/{id}/finalize")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Finalize therapy session", description = "Closes the session. Requires all series Validated. Emits TherapySessionCompleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session finalized."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Session not found."),
            @ApiResponse(responseCode = "409", description = "Routine not completed."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<TherapySessionResource> finalizeTherapySession(@PathVariable UUID id);

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Cancel therapy session", description = "Cancels the session at any non-terminal state. Emits TherapySessionCancelled.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session cancelled."),
            @ApiResponse(responseCode = "400", description = "Invalid input data."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Session not found."),
            @ApiResponse(responseCode = "409", description = "Session already finalized."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<TherapySessionResource> cancelTherapySession(
            @PathVariable UUID id,
            @Valid @RequestBody CancelTherapySessionResource resource);

    @GetMapping("/active/{patientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    @Operation(summary = "Get active session by patient", description = "Returns the Pending/Ready/InProgress session for a patient, if any.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active session found."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "No active session for this patient."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<TherapySessionResource> getActiveTherapySession(@PathVariable UUID patientId);

    @GetMapping("/{id}/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    @Operation(summary = "Get session summary", description = "Returns the executive summary of a completed session.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Session not found."),
            @ApiResponse(responseCode = "409", description = "Session still in progress."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<SessionSummaryResource> getSessionSummary(@PathVariable UUID id);

    @GetMapping("/schedule/{patientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    @Operation(summary = "Get daily schedule by patient",
            description = "Resolves the routine the patient is scheduled to perform on the given date "
                    + "(defaults to today). Returns 200 with a null routineId and zero counters when no "
                    + "active treatment plan covers the date or no routine is scheduled for that day.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Daily schedule resolved (may be empty)."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<DailyScheduleResource> getDailySchedule(
            @PathVariable UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}
