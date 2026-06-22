package com.kiniot.uflex.api.iam.interfaces.rest.controllers;

import com.kiniot.uflex.api.iam.domain.model.commands.RevokeEdgeServiceAccountCommand;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.EdgeServiceAccountId;
import com.kiniot.uflex.api.iam.domain.services.EdgeServiceAccountCommandService;
import com.kiniot.uflex.api.iam.domain.services.EdgeServiceAccountQueryService;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.EdgeServiceAccountCredentialsResource;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.EdgeServiceAccountResource;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.ProvisionEdgeServiceAccountResource;
import com.kiniot.uflex.api.iam.interfaces.rest.transform.EdgeServiceAccountCredentialsResourceFromResultAssembler;
import com.kiniot.uflex.api.iam.interfaces.rest.transform.EdgeServiceAccountResourceFromEntityAssembler;
import com.kiniot.uflex.api.iam.interfaces.rest.transform.ProvisionEdgeServiceAccountCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/iam/edge-service-accounts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Edge Service Accounts", description = "Provisioning of edge (ROLE_EDGE) service accounts")
public class EdgeServiceAccountsController {

    private final EdgeServiceAccountCommandService edgeServiceAccountCommandService;
    private final EdgeServiceAccountQueryService edgeServiceAccountQueryService;

    public EdgeServiceAccountsController(
            EdgeServiceAccountCommandService edgeServiceAccountCommandService,
            EdgeServiceAccountQueryService edgeServiceAccountQueryService) {
        this.edgeServiceAccountCommandService = edgeServiceAccountCommandService;
        this.edgeServiceAccountQueryService = edgeServiceAccountQueryService;
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

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_DEVELOPER')")
    @Operation(summary = "List edge service accounts",
            description = "Returns all provisioned edge service accounts (no credentials). Platform developer only.")
    public ResponseEntity<List<EdgeServiceAccountResource>> list() {
        var resources = edgeServiceAccountQueryService.findAll().stream()
                .map(EdgeServiceAccountResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DEVELOPER')")
    @Operation(summary = "Revoke an edge service account",
            description = "Deletes the edge service account and its ROLE_EDGE user. Platform developer only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Edge service account revoked"),
            @ApiResponse(responseCode = "404", description = "Edge service account not found"),
    })
    public ResponseEntity<Void> revoke(@PathVariable String id) {
        var edgeId = new EdgeServiceAccountId(UUID.fromString(id));
        edgeServiceAccountCommandService.handle(new RevokeEdgeServiceAccountCommand(edgeId));
        return ResponseEntity.noContent().build();
    }
}
