package com.kiniot.uflex.api.organization.interfaces.rest.controllers;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.queries.GetCurrentPhysiotherapistQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistByIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientsByPhysiotherapistIdQuery;
import com.kiniot.uflex.api.organization.domain.services.PatientQueryService;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistsByClinicIdQuery;
import com.kiniot.uflex.api.organization.domain.services.PhysiotherapistCommandService;
import com.kiniot.uflex.api.organization.domain.services.PhysiotherapistQueryService;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.PatientResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.PhysiotherapistResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPhysiotherapistResource;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.PatientResourceFromEntityAssembler;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.PhysiotherapistResourceFromEntityAssembler;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.RegisterPhysiotherapistCommandFromResourceAssembler;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;
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
    private final PatientQueryService patientQueryService;
    private final ExternalIamService externalIamService;

    public PhysiotherapistsController(
            PhysiotherapistCommandService physiotherapistCommandService,
            PhysiotherapistQueryService physiotherapistQueryService,
            PatientQueryService patientQueryService,
            ExternalIamService externalIamService
    ) {
        this.physiotherapistCommandService = physiotherapistCommandService;
        this.physiotherapistQueryService = physiotherapistQueryService;
        this.patientQueryService = patientQueryService;
        this.externalIamService = externalIamService;
    }

    @PostMapping
    @Operation(
            summary = "Register a physiotherapist as a clinic administrator",
            description = "Creates a physiotherapist profile in the authenticated clinic administrator's clinic. "
                    + "This also provisions the IAM user for that physiotherapist using the provided email."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Physiotherapist profile data. `specialty` must be one of the documented enum values, "
                    + "`licenseNumber` must follow the accepted CMP/CTTMP-style format, and the phone must be sent as country code plus local number.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = RegisterPhysiotherapistResource.class),
                    examples = @ExampleObject(
                            name = "Register physiotherapist",
                            value = """
                                    {
                                      "fullName": "Pepito Perez",
                                      "specialty": "NEUROLOGICAL",
                                      "email": "fisio@gmail.com",
                                      "countryCode": "+51",
                                      "phoneNumber": "987654321",
                                      "licenseNumber": "CPT12345",
                                      "professionalSummary": "Fisioterapeuta especializado en rehabilitacion neurologica con mas de 10 anos de experiencia",
                                      "photoUrl": "https://example.com/photos/pepe.jpg",
                                      "yearsOfExperience": 10
                                    }
                                    """
                    )
            )
    )
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
    @Operation(
            summary = "Get a physiotherapist by ID",
            description = "Retrieves a physiotherapist by ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Physiotherapist found"),
            @ApiResponse(responseCode = "404", description = "Physiotherapist not found"),
    })
    public ResponseEntity<PhysiotherapistResource> getPhysiotherapistById(@PathVariable String id) {
        var query = new GetPhysiotherapistByIdQuery(new PhysiotherapistId(UUID.fromString(id)));
        var physiotherapist = physiotherapistQueryService.handle(query);
        if (physiotherapist.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(PhysiotherapistResourceFromEntityAssembler.toResourceFromEntity(physiotherapist.get()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_PHYSIOTHERAPIST')")
    @Operation(
            summary = "Get current physiotherapist profile",
            description = "Returns the physiotherapist profile associated with the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current physiotherapist retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Authenticated physiotherapist profile not found"),
    })
    public ResponseEntity<PhysiotherapistResource> getCurrentPhysiotherapist() {
        return physiotherapistQueryService.handle(new GetCurrentPhysiotherapistQuery())
                .map(PhysiotherapistResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
            summary = "List physiotherapists in the authenticated clinic",
            description = "Returns all physiotherapists who belong to the authenticated user's clinic."
    )
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

    @GetMapping(value = "/me/patients")
    @Operation(
            summary = "List patients assigned to the authenticated physiotherapist",
            description = "Returns all patients assigned to the authenticated physiotherapist."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patients retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Authenticated physiotherapist profile not found"),
    })
    public ResponseEntity<List<PatientResource>> getMyPatients() {
        var physiotherapist = physiotherapistQueryService.handle(new GetCurrentPhysiotherapistQuery())
                .orElseThrow(() -> new UserNotFoundException("Physiotherapist profile not found"));
        var patients = patientQueryService.handle(new GetPatientsByPhysiotherapistIdQuery(physiotherapist.getId()));
        var resources = patients.stream()
                .map(PatientResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
