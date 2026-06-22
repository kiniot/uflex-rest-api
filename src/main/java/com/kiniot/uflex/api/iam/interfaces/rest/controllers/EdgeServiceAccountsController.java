package com.kiniot.uflex.api.iam.interfaces.rest.controllers;

import com.kiniot.uflex.api.iam.domain.services.EdgeServiceAccountCommandService;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.EdgeServiceAccountCredentialsResource;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.ProvisionEdgeServiceAccountResource;
import com.kiniot.uflex.api.iam.interfaces.rest.transform.EdgeServiceAccountCredentialsResourceFromResultAssembler;
import com.kiniot.uflex.api.iam.interfaces.rest.transform.ProvisionEdgeServiceAccountCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/iam/edge-service-accounts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Edge Service Accounts", description = "Provisioning of edge (ROLE_EDGE) service accounts")
public class EdgeServiceAccountsController {

    private final EdgeServiceAccountCommandService edgeServiceAccountCommandService;

    public EdgeServiceAccountsController(EdgeServiceAccountCommandService edgeServiceAccountCommandService) {
        this.edgeServiceAccountCommandService = edgeServiceAccountCommandService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_DEVELOPER', 'ROLE_CLINIC_ADMIN')")
    @Operation(
            summary = "Provision an edge service account",
            description = "Creates a ROLE_EDGE service account bound to the given kit serial. A developer must "
                    + "supply the owning clinicId; a clinic admin may omit it (inferred from their own clinic) and "
                    + "cannot provision for another clinic. Returns the plaintext credential once; it cannot be "
                    + "retrieved again.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Edge service account provisioned"),
            @ApiResponse(responseCode = "409", description = "An edge account already exists for this serial"),
            @ApiResponse(responseCode = "403", description = "Caller lacks permission or targets another clinic"),
    })
    public ResponseEntity<EdgeServiceAccountCredentialsResource> provision(
            @RequestBody ProvisionEdgeServiceAccountResource resource) {
        var command = ProvisionEdgeServiceAccountCommandFromResourceAssembler.toCommandFromResource(resource);
        var credentials = edgeServiceAccountCommandService.handle(command);
        var responseResource = EdgeServiceAccountCredentialsResourceFromResultAssembler.toResourceFromResult(credentials);
        return new ResponseEntity<>(responseResource, HttpStatus.CREATED);
    }
}
