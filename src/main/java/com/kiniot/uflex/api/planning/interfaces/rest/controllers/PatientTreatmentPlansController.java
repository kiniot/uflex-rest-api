package com.kiniot.uflex.api.planning.interfaces.rest.controllers;

import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlansByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanCommandService;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanQueryService;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateTreatmentPlanResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.TreatmentPlanResource;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.CreateTreatmentPlanCommandFromResourceAssembler;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.TreatmentPlanResourceFromEntityAssembler;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/patients/{patientId}/treatment-plans", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Patient Treatment Plans", description = "Treatment plan endpoints scoped to a specific patient")
public class PatientTreatmentPlansController {

    private final TreatmentPlanCommandService treatmentPlanCommandService;
    private final TreatmentPlanQueryService treatmentPlanQueryService;

    public PatientTreatmentPlansController(
            TreatmentPlanCommandService treatmentPlanCommandService,
            TreatmentPlanQueryService treatmentPlanQueryService
    ) {
        this.treatmentPlanCommandService = treatmentPlanCommandService;
        this.treatmentPlanQueryService = treatmentPlanQueryService;
    }

    @GetMapping
    @Operation(
            summary = "Get treatment plans by patient",
            description = "Returns the treatment plans of the specified patient. The patient must exist in the authenticated clinic."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treatment plans retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "409", description = "Patient belongs to a different clinic")
    })
    public ResponseEntity<List<TreatmentPlanResource>> getTreatmentPlansByPatient(@PathVariable String patientId) {
        var treatmentPlans = treatmentPlanQueryService.handle(
                        new GetTreatmentPlansByPatientIdQuery(new PatientId(UUID.fromString(patientId))))
                .stream()
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(treatmentPlans);
    }

    @PostMapping
    @Operation(
            summary = "Create treatment plan for patient",
            description = "Creates a treatment plan for the specified patient. The patient must exist in the authenticated clinic. Scheduled and active plans cannot overlap, and only one active plan is allowed per patient."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Treatment plan data and initial routines.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CreateTreatmentPlanResource.class),
                    examples = @ExampleObject(
                            name = "Create treatment plan for patient",
                            value = """
                                    {
                                      "name": "Forearm mobility plan",
                                      "status": "SCHEDULED",
                                      "period": {
                                        "startsAt": "2026-06-01",
                                        "endsAt": "2026-06-21"
                                      },
                                      "routines": [
                                        {
                                          "name": "Morning mobility",
                                          "order": 1,
                                          "schedule": {
                                            "dayOfWeek": "MONDAY",
                                            "scheduledTime": "08:00:00"
                                          },
                                          "exerciseSeries": [
                                            {
                                              "order": 1,
                                              "exerciseId": "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c",
                                              "rangeOfMotionDegrees": 60,
                                              "repetitions": 12,
                                              "durationSeconds": 45,
                                              "restDurationSeconds": 20
                                            }
                                          ]
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Treatment plan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "409", description = "Patient belongs to a different clinic or the plan conflicts with another scheduled or active plan")
    })
    public ResponseEntity<TreatmentPlanResource> createTreatmentPlan(
            @PathVariable String patientId,
            @RequestBody CreateTreatmentPlanResource resource
    ) {
        try {
            var command = CreateTreatmentPlanCommandFromResourceAssembler.toCommandFromResource(patientId, resource);
            return treatmentPlanCommandService.handle(command)
                    .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                    .map(treatmentPlan -> new ResponseEntity<>(treatmentPlan, HttpStatus.CREATED))
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }
}
