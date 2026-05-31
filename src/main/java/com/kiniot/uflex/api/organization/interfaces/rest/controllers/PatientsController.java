package com.kiniot.uflex.api.organization.interfaces.rest.controllers;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignPatientToPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DischargePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.queries.GetCurrentPatientQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetCurrentPhysiotherapistQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientByIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientsByClinicIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientsByPhysiotherapistIdQuery;
import com.kiniot.uflex.api.organization.domain.services.PatientCommandService;
import com.kiniot.uflex.api.organization.domain.services.PatientQueryService;
import com.kiniot.uflex.api.organization.domain.services.PhysiotherapistQueryService;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.AssignPhysiotherapistResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.PatientResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPatientByClinicAdminResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPatientByPhysiotherapistResource;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.PatientResourceFromEntityAssembler;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.RegisterPatientCommandFromResourceAssembler;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/patients", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Patients", description = "Patient Management Endpoints")
public class PatientsController {

    private final PatientCommandService patientCommandService;
    private final PatientQueryService patientQueryService;
    private final PhysiotherapistQueryService physiotherapistQueryService;
    private final ExternalIamService externalIamService;

    public PatientsController(
            PatientCommandService patientCommandService,
            PatientQueryService patientQueryService,
            PhysiotherapistQueryService physiotherapistQueryService,
            ExternalIamService externalIamService
    ) {
        this.patientCommandService = patientCommandService;
        this.patientQueryService = patientQueryService;
        this.physiotherapistQueryService = physiotherapistQueryService;
        this.externalIamService = externalIamService;
    }

    @PostMapping(value = "/by-clinic-admin")
    @Operation(
            summary = "Register a patient as a clinic administrator",
            description = "Creates a patient profile in the authenticated clinic administrator's clinic. "
                    + "The patient may also be assigned to a physiotherapist from the same clinic."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Patient profile data to register from a clinic administrator context. "
                    + "`gender` must be `MALE`, `FEMALE`, or `OTHER`. "
                    + "If `assignedPhysiotherapistId` is provided, it must belong to the same clinic.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = RegisterPatientByClinicAdminResource.class),
                    examples = @ExampleObject(
                            name = "Clinic admin patient registration",
                            value = """
                                    {
                                      "firstName": "Lucia",
                                      "lastName": "Ramos",
                                      "dni": "74839210",
                                      "birthDate": "1992-08-14",
                                      "gender": "FEMALE",
                                      "email": "lucia.ramos@example.com",
                                      "countryCode": "+51",
                                      "phoneNumber": "987654321",
                                      "medicalCondition": "Post-operative knee rehabilitation",
                                      "assignedPhysiotherapistId": "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Assigned physiotherapist not found"),
            @ApiResponse(responseCode = "409", description = "The authenticated user already has a registered patient profile"),
    })
    @PreAuthorize("hasAuthority('ROLE_CLINIC_ADMIN')")
    public ResponseEntity<PatientResource> registerPatient(@RequestBody RegisterPatientByClinicAdminResource resource) {
        var command = RegisterPatientCommandFromResourceAssembler.toRegisterPatientByClinicAdminCommand(resource);
        var patient = patientCommandService.handle(command);
        if (patient.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var patientResource = PatientResourceFromEntityAssembler.toResourceFromEntity(patient.get());
        return new ResponseEntity<>(patientResource, HttpStatus.CREATED);
    }

    @PostMapping(value = "/by-physiotherapist")
    @Operation(
            summary = "Register a patient as a physiotherapist",
            description = "Creates a patient profile in the authenticated physiotherapist's clinic and automatically assigns "
                    + "the patient to that physiotherapist."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Patient profile data to register from a physiotherapist context. "
                    + "`gender` must be `MALE`, `FEMALE`, or `OTHER`. "
                    + "The physiotherapist assignment is inferred from the authenticated user.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = RegisterPatientByPhysiotherapistResource.class),
                    examples = @ExampleObject(
                            name = "Physiotherapist patient registration",
                            value = """
                                    {
                                      "firstName": "Mateo",
                                      "lastName": "Salazar",
                                      "dni": "73124568",
                                      "birthDate": "1987-03-22",
                                      "gender": "MALE",
                                      "email": "mateo.salazar@example.com",
                                      "countryCode": "+51",
                                      "phoneNumber": "912345678",
                                      "medicalCondition": "Shoulder mobility recovery"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient created and assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Authenticated physiotherapist profile not found"),
            @ApiResponse(responseCode = "409", description = "The authenticated user already has a registered patient profile"),
    })
    @PreAuthorize("hasAuthority('ROLE_PHYSIOTHERAPIST')")
    public ResponseEntity<PatientResource> registerPatientByPhysiotherapist(@RequestBody RegisterPatientByPhysiotherapistResource resource) {
        var physiotherapist = physiotherapistQueryService.handle(new GetCurrentPhysiotherapistQuery())
                .orElseThrow(() -> new UserNotFoundException("Physiotherapist profile not found"));

        var command = RegisterPatientCommandFromResourceAssembler.toRegisterPatientByPhysiotherapistCommand(resource, physiotherapist.getId());
        var patient = patientCommandService.handle(command);
        if (patient.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var patientResource = PatientResourceFromEntityAssembler.toResourceFromEntity(patient.get());
        return new ResponseEntity<>(patientResource, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}")
    @Operation(
            summary = "Get a patient by ID",
            description = "Retrieves a patient by ID. This endpoint is intended for clinic administrators and physiotherapists."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
    })
    public ResponseEntity<PatientResource> getPatientById(@PathVariable String id) {
        var query = new GetPatientByIdQuery(new PatientId(UUID.fromString(id)));
        var patient = patientQueryService.handle(query);
        if (patient.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(PatientResourceFromEntityAssembler.toResourceFromEntity(patient.get()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @Operation(
            summary = "Get current patient profile",
            description = "Returns the patient profile associated with the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current patient retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Authenticated patient profile not found"),
    })
    public ResponseEntity<PatientResource> getCurrentPatient() {
        return patientQueryService.handle(new GetCurrentPatientQuery())
                .map(PatientResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
            summary = "List patients in the authenticated clinic",
            description = "Returns all patients who belong to the authenticated clinic administrator's clinic."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patients retrieved successfully"),
    })
    public ResponseEntity<List<PatientResource>> getPatientsForClinic() {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException("Current clinic not found"));
        var patients = patientQueryService.handle(new GetPatientsByClinicIdQuery(clinicId));
        var resources = patients.stream()
                .map(PatientResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping(value = "/by-clinic/{clinicId}")
    @Operation(
            summary = "List patients by clinic ID",
            description = "Returns all patients who belong to the specified clinic. This endpoint is intended for clinic administrators."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patients retrieved successfully"),
    })
    public ResponseEntity<List<PatientResource>> getPatientsByClinic(@PathVariable String clinicId) {
        var query = new GetPatientsByClinicIdQuery(new ClinicId(UUID.fromString(clinicId)));
        var patients = patientQueryService.handle(query);
        var resources = patients.stream()
                .map(PatientResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping(value = "/by-physiotherapist/{physiotherapistId}")
    @Operation(
            summary = "List patients by physiotherapist ID",
            description = "Returns all patients assigned to the specified physiotherapist. This endpoint is intended for clinic administrators and physiotherapists."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patients retrieved successfully"),
    })
    public ResponseEntity<List<PatientResource>> getPatientsByPhysiotherapist(@PathVariable String physiotherapistId) {
        var query = new GetPatientsByPhysiotherapistIdQuery(new PhysiotherapistId(UUID.fromString(physiotherapistId)));
        var patients = patientQueryService.handle(query);
        var resources = patients.stream()
                .map(PatientResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping(value = "/{id}/assign")
    @Operation(
            summary = "Assign a patient to a physiotherapist",
            description = "Assigns a patient to a physiotherapist within the same clinic. This endpoint is intended for clinic administrators."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or patient not found"),
    })
    public ResponseEntity<Void> assignPatientToPhysiotherapist(
            @PathVariable String id,
            @RequestBody AssignPhysiotherapistResource resource
    ) {
        var patientId = new PatientId(UUID.fromString(id));
        var physiotherapistId = new PhysiotherapistId(UUID.fromString(resource.physiotherapistId()));
        var command = new AssignPatientToPhysiotherapistCommand(patientId, physiotherapistId);
        patientCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}/discharge")
    @Operation(
            summary = "Discharge a patient",
            description = "Marks a patient as discharged. The authenticated physiotherapist may discharge only patients assigned to them."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient discharged successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or patient not found"),
    })
    public ResponseEntity<Void> dischargePatient(@PathVariable String id) {
        var physiotherapist = physiotherapistQueryService.handle(new GetCurrentPhysiotherapistQuery())
                .orElseThrow(() -> new UserNotFoundException("Physiotherapist profile not found"));

        var patient = patientQueryService.handle(new GetPatientByIdQuery(new PatientId(UUID.fromString(id))))
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        if (patient.getAssignedPhysiotherapistId() == null ||
            !patient.getAssignedPhysiotherapistId().equals(physiotherapist.getId())) {
            throw new IllegalStateException("You can only discharge your own patients");
        }

        var command = new DischargePatientCommand(new PatientId(UUID.fromString(id)));
        patientCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
