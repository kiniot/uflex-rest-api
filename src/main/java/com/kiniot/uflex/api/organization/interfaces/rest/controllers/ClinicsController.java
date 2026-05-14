package com.kiniot.uflex.api.organization.interfaces.rest.controllers;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.organization.domain.services.ClinicCommandService;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.ClinicResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterClinicResource;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.ClinicResourceFromEntityAssembler;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.RegisterClinicCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/clinics", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clinics", description = "Clinic Management Endpoints")
public class ClinicsController {

    private final ClinicCommandService clinicCommandService;
    private final ExternalIamService externalIamService;

    public ClinicsController(ClinicCommandService clinicCommandService, ExternalIamService externalIamService) {
        this.clinicCommandService = clinicCommandService;
        this.externalIamService = externalIamService;
    }

    @PostMapping
    @Operation(summary = "Register a new clinic", description = "Creates a new clinic with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Clinic created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
    })
    public ResponseEntity<ClinicResource> registerClinic(@RequestBody RegisterClinicResource resource) {
        var userId = externalIamService.fetchCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        var command = RegisterClinicCommandFromResourceAssembler.toCommandFromResource(resource, userId);
        var clinic = clinicCommandService.handle(command);
        if (clinic.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var clinicResource = ClinicResourceFromEntityAssembler.toResourceFromEntity(clinic.get());
        return new ResponseEntity<>(clinicResource, HttpStatus.CREATED);
    }
}
