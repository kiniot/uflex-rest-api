package com.kiniot.uflex.api.organization.interfaces.rest.controllers;

import com.kiniot.uflex.api.organization.domain.model.queries.GetCurrentClinicAdminQuery;
import com.kiniot.uflex.api.organization.domain.services.ClinicAdminCommandService;
import com.kiniot.uflex.api.organization.domain.services.ClinicAdminQueryService;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.ClinicAdminResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterClinicAdminResource;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.ClinicAdminResourceFromEntityAssembler;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.RegisterClinicAdminCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/clinic-admins", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clinic Admins", description = "Clinic Admin Management Endpoints")
public class ClinicAdminsController {

    private final ClinicAdminCommandService clinicAdminCommandService;
    private final ClinicAdminQueryService clinicAdminQueryService;

    public ClinicAdminsController(
            ClinicAdminCommandService clinicAdminCommandService,
            ClinicAdminQueryService clinicAdminQueryService
    ) {
        this.clinicAdminCommandService = clinicAdminCommandService;
        this.clinicAdminQueryService = clinicAdminQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Register a new clinic admin",
            description = "Creates the clinic administrator profile for the authenticated user in their current clinic. "
                    + "The email is taken from the authenticated IAM user, while the request provides personal and contact data."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Clinic administrator profile data. `dni` must contain exactly 8 digits, "
                    + "`gender` must be `MALE`, `FEMALE`, or `OTHER`, and the phone must be sent as country code plus local number.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = RegisterClinicAdminResource.class),
                    examples = @ExampleObject(
                            name = "Register clinic admin",
                            value = """
                                    {
                                      "firstName": "Mariana",
                                      "lastName": "Lopez",
                                      "dni": "74839210",
                                      "birthDate": "1990-04-12",
                                      "gender": "FEMALE",
                                      "countryCode": "+51",
                                      "phoneNumber": "987654321"
                                    }
                                    """
                    )
            )
    )
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

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_CLINIC_ADMIN')")
    @Operation(
            summary = "Get current clinic admin profile",
            description = "Returns the clinic administrator profile associated with the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current clinic admin retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Authenticated clinic admin profile not found")
    })
    public ResponseEntity<ClinicAdminResource> getCurrentClinicAdmin() {
        return clinicAdminQueryService.handle(new GetCurrentClinicAdminQuery())
                .map(ClinicAdminResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
