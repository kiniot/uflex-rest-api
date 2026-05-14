package com.kiniot.uflex.api.organization.interfaces.rest.controllers;

import com.kiniot.uflex.api.organization.domain.model.entities.ClinicAdmin;
import com.kiniot.uflex.api.organization.domain.services.ClinicAdminCommandService;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.ClinicAdminResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterClinicAdminResource;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.ClinicAdminResourceFromEntityAssembler;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.RegisterClinicAdminCommandFromResourceAssembler;
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
@RequestMapping(value = "/api/v1/clinic-admins", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clinic Admins", description = "Clinic Admin Management Endpoints")
public class ClinicAdminsController {

    private final ClinicAdminCommandService clinicAdminCommandService;

    public ClinicAdminsController(ClinicAdminCommandService clinicAdminCommandService) {
        this.clinicAdminCommandService = clinicAdminCommandService;
    }

    @PostMapping
    @Operation(summary = "Register a new clinic admin", description = "Creates a new clinic admin profile for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Clinic admin created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
    })
    public ResponseEntity<ClinicAdminResource> registerClinicAdmin(@RequestBody RegisterClinicAdminResource resource) {
        var command = RegisterClinicAdminCommandFromResourceAssembler.toCommandFromResource(resource);
        var clinicAdmin = clinicAdminCommandService.handle(command);
        if (clinicAdmin.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var clinicAdminResource = ClinicAdminResourceFromEntityAssembler.toResourceFromEntity(clinicAdmin.get());
        return new ResponseEntity<>(clinicAdminResource, HttpStatus.CREATED);
    }
}