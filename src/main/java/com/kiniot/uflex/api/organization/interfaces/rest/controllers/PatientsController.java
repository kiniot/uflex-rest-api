package com.kiniot.uflex.api.organization.interfaces.rest.controllers;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignTreatmentPlanToPatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DischargePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientByIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientByUserIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientsByClinicIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientsByPhysiotherapistIdQuery;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.organization.domain.services.PatientCommandService;
import com.kiniot.uflex.api.organization.domain.services.PatientQueryService;
import com.kiniot.uflex.api.organization.domain.services.PhysiotherapistQueryService;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.AssignTreatmentPlanResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.PatientResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPatientResource;
import com.kiniot.uflex.api.organization.interfaces.rest.transform.AssignTreatmentPlanCommandFromResourceAssembler;
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
    @Operation(summary = "Register a new patient", description = "Creates a new patient profile")
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

    @GetMapping(value = "/{id}")
    @Operation(summary = "Get patient by ID", description = "Retrieves a patient by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
    })
    public ResponseEntity<PatientResource> getPatientById(@PathVariable String id) {
        var query = new GetPatientByIdQuery(new com.kiniot.uflex.api.organization.domain.model.valueobjects.PatientId(UUID.fromString(id)));
        var patient = patientQueryService.handle(query);
        if (patient.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(PatientResourceFromEntityAssembler.toResourceFromEntity(patient.get()));
    }

    @GetMapping
    @Operation(summary = "Get all patients for current physiotherapist", description = "Retrieves all patients assigned to the authenticated physiotherapist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patients retrieved successfully"),
    })
    public ResponseEntity<List<PatientResource>> getMyPatients() {
        var userId = externalIamService.fetchCurrentUserId()
                .orElseThrow(() -> new com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException("Current user not found"));
        var physiotherapist = physiotherapistQueryService.handle(new com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistByUserIdQuery(userId))
                .orElseThrow(() -> new com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException("Physiotherapist profile not found"));
        var patients = patientQueryService.handle(new GetPatientsByPhysiotherapistIdQuery(physiotherapist.getId()));
        var resources = patients.stream()
                .map(PatientResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping(value = "/by-clinic/{clinicId}")
    @Operation(summary = "Get all patients by clinic ID", description = "Retrieves all patients belonging to a specific clinic")
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
    @Operation(summary = "Get all patients by physiotherapist ID", description = "Retrieves all patients assigned to a specific physiotherapist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patients retrieved successfully"),
    })
    public ResponseEntity<List<PatientResource>> getPatientsByPhysiotherapist(@PathVariable String physiotherapistId) {
        var query = new GetPatientsByPhysiotherapistIdQuery(
                new com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistId(UUID.fromString(physiotherapistId)));
        var patients = patientQueryService.handle(query);
        var resources = patients.stream()
                .map(PatientResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping(value = "/{id}/assign")
    @Operation(summary = "Assign treatment plan to patient", description = "Assigns a treatment plan to an existing patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Treatment plan assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or patient not found"),
    })
    public ResponseEntity<Void> assignTreatmentPlan(
            @PathVariable String id,
            @RequestBody AssignTreatmentPlanResource resource
    ) {
        var command = AssignTreatmentPlanCommandFromResourceAssembler.toCommandFromResource(id, resource);
        patientCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}/discharge")
    @Operation(summary = "Discharge patient", description = "Changes patient status to DISCHARGED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient discharged successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or patient not found"),
    })
    public ResponseEntity<Void> dischargePatient(@PathVariable String id) {
        var command = new DischargePatientCommand(
                new com.kiniot.uflex.api.organization.domain.model.valueobjects.PatientId(UUID.fromString(id)));
        patientCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}