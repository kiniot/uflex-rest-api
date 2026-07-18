package com.kiniot.uflex.api.therapy.interfaces.rest.swagger;

import com.kiniot.uflex.api.therapy.interfaces.rest.resources.TherapySessionHistoryItemResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Clinician-facing history of a patient's therapy sessions.
 *
 * <p>Lives in the therapy context despite the {@code /patients/...} path, mirroring
 * {@code planning}'s {@code PatientTreatmentPlansController}. The explicit-{@code patientId} form is
 * free to use: the mobile app only ever calls {@code /patients/me/**}.
 */
@RequestMapping(value = "/api/v1/patients/{patientId}/therapy-sessions", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Therapy Sessions", description = "Therapy Session Lifecycle Endpoints")
public interface PatientTherapySessionsController {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "List a patient's therapy session history",
            description = "Returns the patient's sessions, newest first, each with its aggregates "
                    + "(series/repetition counts, average achieved ROM, pain, compensatory movements) "
                    + "already computed. Pass treatmentPlanId to scope the history to one plan, which "
                    + "is what a plan's ROM trend plots. Results are capped at 500 and returned as a "
                    + "plain array, not a page: the client charts the whole series, and a page would "
                    + "silently truncate the trend line.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History retrieved (may be empty)."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<List<TherapySessionHistoryItemResource>> getTherapySessionHistory(
            @PathVariable UUID patientId,
            @RequestParam(required = false) UUID treatmentPlanId);
}
