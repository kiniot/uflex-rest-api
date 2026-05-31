package com.kiniot.uflex.api.organization.interfaces.rest.controllers;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.model.queries.GetCurrentClinicQuery;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.organization.domain.services.ClinicCommandService;
import com.kiniot.uflex.api.organization.domain.services.ClinicQueryService;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.ClinicResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterClinicResource;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.ClinicResourceFromEntityAssembler;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.RegisterClinicCommandFromResourceAssembler;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/clinics", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clinics", description = "Clinic Management Endpoints")
public class ClinicsController {

    private final ClinicCommandService clinicCommandService;
    private final ClinicQueryService clinicQueryService;
    private final ExternalIamService externalIamService;

    public ClinicsController(
            ClinicCommandService clinicCommandService,
            ClinicQueryService clinicQueryService,
            ExternalIamService externalIamService
    ) {
        this.clinicCommandService = clinicCommandService;
        this.clinicQueryService = clinicQueryService;
        this.externalIamService = externalIamService;
    }

    @PostMapping
    @Operation(
            summary = "Register a new clinic",
            description = "Creates a new clinic for the authenticated user. "
                    + "The authenticated user becomes the owner context for the clinic and the clinic data must satisfy the organization domain validations."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Clinic registration data. `ruc` must contain exactly 11 digits, "
                    + "`legalName` and `commercialName` are required, the phone must be sent as country code plus local number, "
                    + "and `address.countryCode` must be a valid ISO 3166-1 alpha-2 code such as `PE` or `US`. "
                    + "`address.addressLine2` and `address.postalCode` are optional.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = RegisterClinicResource.class),
                    examples = @ExampleObject(
                            name = "Register clinic",
                            value = """
                                    {
                                      "legalName": "Centro de Rehabilitacion Integral S.A.C.",
                                      "commercialName": "Rehab Center Lima",
                                      "ruc": "20601234567",
                                      "email": "contacto@rehabcenter.pe",
                                      "countryCode": "+51",
                                      "phoneNumber": "987654321",
                                      "address": {
                                        "countryCode": "PE",
                                        "region": "Lima",
                                        "city": "Lima",
                                        "addressLine1": "Av. Javier Prado Este 1234",
                                        "addressLine2": "Piso 4",
                                        "postalCode": "15036"
                                      }
                                    }
                                    """
                    )
            )
    )
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

    @GetMapping("/me")
    @Operation(
            summary = "Get current clinic",
            description = "Returns the clinic associated with the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current clinic retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Authenticated user has no associated clinic")
    })
    public ResponseEntity<ClinicResource> getCurrentClinic() {
        return clinicQueryService.handle(new GetCurrentClinicQuery())
                .map(ClinicResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
