package com.kiniot.uflex.api.organization.interfaces.rest.controllers;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignPatientToPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DischargePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientByIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientsByClinicIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientsByPhysiotherapistIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistByUserIdQuery;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import com.kiniot.uflex.api.organization.domain.services.PatientCommandService;
import com.kiniot.uflex.api.organization.domain.services.PatientQueryService;
import com.kiniot.uflex.api.organization.domain.services.PhysiotherapistQueryService;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.AssignPhysiotherapistResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.PatientResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPatientResource;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.PatientResourceFromEntityAssembler;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.RegisterPatientCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    @Operation(summary = "Register a new patient", description = "CLINIC ADMIN: Creates a new patient profile with optional physiotherapist assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
    })
    public ResponseEntity<PatientResource> registerPatient(@RequestBody RegisterPatientResource resource) {
        var command = RegisterPatientCommandFromResourceAssembler.toCommandFromResource(resource);
        var patient = patientCommandService.handle(command);
        if (patient.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var patientResource = PatientResourceFromEntityAssembler.toResourceFromEntity(patient.get());
        return new ResponseEntity<>(patientResource, HttpStatus.CREATED);
    }

    @PostMapping(value = "/by-physiotherapist")
    @Operation(summary = "Register new patient assigned to current physiotherapist", description = "PHYSIOTHERAPIST: Creates a new patient profile and assigns it to the authenticated physiotherapist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient created and assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
    })
    public ResponseEntity<PatientResource> registerPatientByPhysiotherapist(@RequestBody RegisterPatientResource resource) {
        var userId = externalIamService.fetchCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        var physiotherapist = physiotherapistQueryService.handle(new GetPhysiotherapistByUserIdQuery(userId))
                .orElseThrow(() -> new UserNotFoundException("Physiotherapist profile not found"));

        var command = RegisterPatientCommandFromResourceAssembler.toCommandFromResource(resource, physiotherapist.getId());
        var patient = patientCommandService.handle(command);
        if (patient.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var patientResource = PatientResourceFromEntityAssembler.toResourceFromEntity(patient.get());
        return new ResponseEntity<>(patientResource, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Get patient by ID", description = "CLINIC ADMIN or PHYSIOTHERAPIST: Retrieves a patient by their ID")
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

    @GetMapping(value = "/my")
    @Operation(summary = "Get my patients", description = "PHYSIOTHERAPIST: Retrieves all patients assigned to the authenticated physiotherapist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patients retrieved successfully"),
    })
    public ResponseEntity<List<PatientResource>> getMyPatients() {
        var userId = externalIamService.fetchCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        var physiotherapist = physiotherapistQueryService.handle(new GetPhysiotherapistByUserIdQuery(userId))
                .orElseThrow(() -> new UserNotFoundException("Physiotherapist profile not found"));
        var patients = patientQueryService.handle(new GetPatientsByPhysiotherapistIdQuery(physiotherapist.getId()));
        var resources = patients.stream()
                .map(PatientResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping
    @Operation(summary = "Get all patients for clinic", description = "CLINIC ADMIN: Retrieves all patients belonging to the admin's clinic")
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
    @Operation(summary = "Get all patients by clinic ID", description = "CLINIC ADMIN: Retrieves all patients belonging to a specific clinic")
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
    @Operation(summary = "Get all patients by physiotherapist ID", description = "CLINIC ADMIN or PHYSIOTHERAPIST: Retrieves all patients assigned to a specific physiotherapist")
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
    @Operation(summary = "Assign patient to physiotherapist", description = "CLINIC ADMIN: Assigns a patient to a physiotherapist within the same clinic")
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
    @Operation(summary = "Discharge patient", description = "PHYSIOTHERAPIST: Changes patient status to DISCHARGED (only own patients)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient discharged successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or patient not found"),
    })
    public ResponseEntity<Void> dischargePatient(@PathVariable String id) {
        var userId = externalIamService.fetchCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        var physiotherapist = physiotherapistQueryService.handle(new GetPhysiotherapistByUserIdQuery(userId))
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