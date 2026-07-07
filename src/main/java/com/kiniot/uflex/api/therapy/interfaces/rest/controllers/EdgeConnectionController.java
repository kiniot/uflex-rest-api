package com.kiniot.uflex.api.therapy.interfaces.rest.controllers;

import com.kiniot.uflex.api.therapy.domain.model.queries.GetEdgeConnectionForCurrentPatientQuery;
import com.kiniot.uflex.api.therapy.domain.services.TherapySessionQueryService;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.EdgeConnectionResource;
import com.kiniot.uflex.api.therapy.interfaces.rest.transform.EdgeConnectionResourceFromResultAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/patients/me/edge-connection", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Edge Connection", description = "Rendezvous: where the patient's mobile app reaches its edge on the LAN")
public class EdgeConnectionController {

    private final TherapySessionQueryService therapySessionQueryService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @Operation(
            summary = "Get edge connection info for the current patient",
            description = "Returns the LAN URL and pairing token the mobile app uses to subscribe to its "
                    + "edge's live SSE progress stream. The pairing token is tied to the patient's active "
                    + "therapy session; localEdgeUrl may be null if the edge has not reported its LAN URL yet "
                    + "(the app then falls back to its build-time default).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Edge connection info resolved."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "No active session for this patient."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<EdgeConnectionResource> getEdgeConnection() {
        var connection = therapySessionQueryService.handle(new GetEdgeConnectionForCurrentPatientQuery());
        return ResponseEntity.ok(EdgeConnectionResourceFromResultAssembler.toResourceFromResult(connection));
    }
}
