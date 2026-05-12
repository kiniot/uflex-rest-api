package com.kiniot.uflex.api.organization.interfaces.rest.controllers;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistByIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistsByClinicIdQuery;
import com.kiniot.uflex.api.organization.domain.services.PhysiotherapistCommandService;
import com.kiniot.uflex.api.organization.domain.services.PhysiotherapistQueryService;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.PhysiotherapistResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPhysiotherapistResource;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.PhysiotherapistResourceFromEntityAssembler;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.RegisterPhysiotherapistCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/physiotherapists", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Physiotherapists", description = "Physiotherapist Management Endpoints")
public class PhysiotherapistsController {

    private final PhysiotherapistCommandService physiotherapistCommandService;
    private final PhysiotherapistQueryService physiotherapistQueryService;
    private final ExternalIamService externalIamService;

    public PhysiotherapistsController(
            PhysiotherapistCommandService physiotherapistCommandService,
            PhysiotherapistQueryService physiotherapistQueryService,
            ExternalIamService externalIamService
    ) {
        this.physiotherapistCommandService = physiotherapistCommandService;
        this.physiotherapistQueryService = physiotherapistQueryService;
        this.externalIamService = externalIamService;
    }

    @PostMapping
    @Operation(summary = "Register a new physiotherapist", description = "Creates a new physiotherapist profile for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Physiotherapist created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
    })
    public ResponseEntity<PhysiotherapistResource> registerPhysiotherapist(@RequestBody RegisterPhysiotherapistResource resource) {
        var command = RegisterPhysiotherapistCommandFromResourceAssembler.toCommandFromResource(resource);
        var physiotherapist = physiotherapistCommandService.handle(command);
        if (physiotherapist.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var physioResource = PhysiotherapistResourceFromEntityAssembler.toResourceFromEntity(physiotherapist.get());
        return new ResponseEntity<>(physioResource, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Get physiotherapist by ID", description = "Retrieves a physiotherapist by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Physiotherapist found"),
            @ApiResponse(responseCode = "404", description = "Physiotherapist not found"),
    })
    public ResponseEntity<PhysiotherapistResource> getPhysiotherapistById(@PathVariable String id) {
        var query = new GetPhysiotherapistByIdQuery(new com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistId(UUID.fromString(id)));
        var physiotherapist = physiotherapistQueryService.handle(query);
        if (physiotherapist.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(PhysiotherapistResourceFromEntityAssembler.toResourceFromEntity(physiotherapist.get()));
    }

    @GetMapping
    @Operation(summary = "Get all physiotherapists for current clinic", description = "Retrieves all physiotherapists belonging to the authenticated user's clinic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Physiotherapists retrieved successfully"),
    })
    public ResponseEntity<List<PhysiotherapistResource>> getPhysiotherapistsByClinic() {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ClinicNotFoundException("Current clinic not found"));
        var query = new GetPhysiotherapistsByClinicIdQuery(clinicId);
        var physiotherapists = physiotherapistQueryService.handle(query);
        var resources = physiotherapists.stream()
                .map(PhysiotherapistResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}