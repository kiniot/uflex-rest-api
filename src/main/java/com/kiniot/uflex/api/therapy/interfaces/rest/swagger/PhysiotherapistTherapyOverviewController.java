package com.kiniot.uflex.api.therapy.interfaces.rest.swagger;

import com.kiniot.uflex.api.therapy.interfaces.rest.resources.PatientTherapyOverviewResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The physiotherapist's therapy index: every patient assigned to them, with how their therapy is
 * going, in one request.
 *
 * <p>Lives in the therapy context despite the {@code /physiotherapists/...} path, same as
 * {@code PatientTherapySessionsController}: the path names the audience, the data is therapy's.
 * Answering this per patient would be a request each.
 */
@RequestMapping(value = "/api/v1/physiotherapists/me/patients/therapy-overview", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Therapy Sessions", description = "Therapy Session Lifecycle Endpoints")
public interface PhysiotherapistTherapyOverviewController {

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Therapy overview of the current physiotherapist's patients",
            description = "Returns one row per assigned patient with their session counts, last "
                    + "session, repetition quality, average achieved ROM, how many sessions need "
                    + "clinical review, and whether they are mid-session right now. Patients who "
                    + "have never started a session are included with zeroes — an untouched "
                    + "caseload is the point of the list. Ordered by last session, never-started "
                    + "first.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overview retrieved (may be empty)."),
            @ApiResponse(responseCode = "403", description = "Caller has no physiotherapist profile."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    ResponseEntity<List<PatientTherapyOverviewResource>> getPatientTherapyOverview();
}
