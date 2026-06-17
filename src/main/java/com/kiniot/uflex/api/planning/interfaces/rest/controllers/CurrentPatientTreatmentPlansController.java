package com.kiniot.uflex.api.planning.interfaces.rest.controllers;

import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.planning.domain.model.queries.GetActiveTreatmentPlanByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetNextScheduledTreatmentPlanByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetScheduledTreatmentPlansByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlanByPatientIdAndTreatmentPlanIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlansByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanQueryService;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.TreatmentPlanResource;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.TreatmentPlanResourceFromEntityAssembler;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/patients/me/treatment-plans", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Current Patient Treatment Plans", description = "Treatment plan endpoints for the authenticated patient")
public class CurrentPatientTreatmentPlansController {

    private final TreatmentPlanQueryService treatmentPlanQueryService;
    private final ExternalIamService externalIamService;
    private final ExternalOrganizationService externalOrganizationService;

    public CurrentPatientTreatmentPlansController(
            TreatmentPlanQueryService treatmentPlanQueryService,
            ExternalIamService externalIamService,
            ExternalOrganizationService externalOrganizationService
    ) {
        this.treatmentPlanQueryService = treatmentPlanQueryService;
        this.externalIamService = externalIamService;
        this.externalOrganizationService = externalOrganizationService;
    }

    @GetMapping
    @Operation(summary = "Get my treatment plans", description = "Returns the treatment plans of the authenticated patient.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treatment plans retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Authenticated patient profile not found")
    })
    public ResponseEntity<List<TreatmentPlanResource>> getTreatmentPlans() {
        var patientId = resolveCurrentPatientId();
        var treatmentPlans = treatmentPlanQueryService.handle(new GetTreatmentPlansByPatientIdQuery(patientId)).stream()
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(treatmentPlans);
    }

    @GetMapping("/active")
    @Operation(summary = "Get my active treatment plan", description = "Returns the active treatment plan of the authenticated patient.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active treatment plan retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Authenticated patient profile or active treatment plan not found")
    })
    public ResponseEntity<TreatmentPlanResource> getActiveTreatmentPlan() {
        var patientId = resolveCurrentPatientId();
        return treatmentPlanQueryService.handle(new GetActiveTreatmentPlanByPatientIdQuery(patientId))
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/scheduled")
    @Operation(summary = "Get my scheduled treatment plans", description = "Returns the scheduled treatment plans of the authenticated patient.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheduled treatment plans retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Authenticated patient profile not found")
    })
    public ResponseEntity<List<TreatmentPlanResource>> getScheduledTreatmentPlans() {
        var patientId = resolveCurrentPatientId();
        var treatmentPlans = treatmentPlanQueryService.handle(new GetScheduledTreatmentPlansByPatientIdQuery(patientId)).stream()
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(treatmentPlans);
    }

    @GetMapping("/next-scheduled")
    @Operation(summary = "Get my next scheduled treatment plan", description = "Returns the next scheduled treatment plan of the authenticated patient.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Next scheduled treatment plan retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No next scheduled treatment plan exists"),
            @ApiResponse(responseCode = "404", description = "Authenticated patient profile not found")
    })
    public ResponseEntity<TreatmentPlanResource> getNextScheduledTreatmentPlan() {
        var patientId = resolveCurrentPatientId();
        return treatmentPlanQueryService.handle(new GetNextScheduledTreatmentPlanByPatientIdQuery(patientId))
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{treatmentPlanId}")
    @Operation(summary = "Get my treatment plan by id", description = "Returns the specified treatment plan only if it belongs to the authenticated patient.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treatment plan retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Authenticated patient profile or treatment plan not found")
    })
    public ResponseEntity<TreatmentPlanResource> getTreatmentPlanById(@PathVariable String treatmentPlanId) {
        var patientId = resolveCurrentPatientId();
        return treatmentPlanQueryService.handle(
                        new GetTreatmentPlanByPatientIdAndTreatmentPlanIdQuery(
                                patientId,
                                new TreatmentPlanId(UUID.fromString(treatmentPlanId))
                        ))
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId resolveCurrentPatientId() {
        var currentUserId = externalIamService.fetchCurrentUserId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return externalOrganizationService.findPatientIdByUserId(currentUserId);
    }
}
