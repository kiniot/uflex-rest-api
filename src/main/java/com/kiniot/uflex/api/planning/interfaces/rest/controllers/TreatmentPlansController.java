package com.kiniot.uflex.api.planning.interfaces.rest.controllers;

import com.kiniot.uflex.api.planning.domain.model.commands.ActivateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CancelTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CompleteTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.queries.GetAllTreatmentPlansQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlanByIdQuery;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanStatus;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanCommandService;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanQueryService;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateRoutineResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.TreatmentPlanResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.UpdateRoutineResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.UpdateTreatmentPlanResource;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.CreateRoutineCommandFromResourceAssembler;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.TreatmentPlanResourceFromEntityAssembler;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.UpdateRoutineCommandFromResourceAssembler;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.UpdateTreatmentPlanCommandFromResourceAssembler;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/treatment-plans", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Treatment Plans", description = "Available treatment plan endpoints")
public class TreatmentPlansController {

    private final TreatmentPlanCommandService treatmentPlanCommandService;
    private final TreatmentPlanQueryService treatmentPlanQueryService;

    public TreatmentPlansController(TreatmentPlanCommandService treatmentPlanCommandService,
                                    TreatmentPlanQueryService treatmentPlanQueryService) {
        this.treatmentPlanCommandService = treatmentPlanCommandService;
        this.treatmentPlanQueryService = treatmentPlanQueryService;
    }

    @GetMapping
    @Operation(summary = "Get treatment plans",
            description = "Returns treatment plans of the authenticated clinic. Optional filters: patientId, physiotherapistId, status, startsAtFrom/startsAtTo, and endsAtFrom/endsAtTo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treatment plans retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or authenticated user has no clinic")
    })
    public ResponseEntity<List<TreatmentPlanResource>> getTreatmentPlans(
            @Parameter(description = "Filter by patient id", example = "019e7e3d-61cb-73fc-990f-91dc6c19a3fa")
            @RequestParam(required = false) String patientId,
            @Parameter(description = "Filter by physiotherapist id", example = "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c")
            @RequestParam(required = false) String physiotherapistId,
            @Parameter(description = "Filter by treatment plan status", schema = @Schema(allowableValues = {"SCHEDULED", "ACTIVE", "COMPLETED", "CANCELED"}))
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter plans whose startsAt is on or after this date", example = "2026-06-01")
            @RequestParam(required = false) LocalDate startsAtFrom,
            @Parameter(description = "Filter plans whose startsAt is on or before this date", example = "2026-06-30")
            @RequestParam(required = false) LocalDate startsAtTo,
            @Parameter(description = "Filter plans whose endsAt is on or after this date", example = "2026-06-01")
            @RequestParam(required = false) LocalDate endsAtFrom,
            @Parameter(description = "Filter plans whose endsAt is on or before this date", example = "2026-06-30")
            @RequestParam(required = false) LocalDate endsAtTo
    ) {
        var query = new GetAllTreatmentPlansQuery(
                patientId != null && !patientId.isBlank() ? new PatientId(UUID.fromString(patientId)) : null,
                physiotherapistId != null && !physiotherapistId.isBlank() ? new PhysiotherapistId(UUID.fromString(physiotherapistId)) : null,
                status != null && !status.isBlank() ? TreatmentPlanStatus.valueOf(status) : null,
                startsAtFrom,
                startsAtTo,
                endsAtFrom,
                endsAtTo
        );
        var treatmentPlans = treatmentPlanQueryService.handle(query).stream()
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(treatmentPlans);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get treatment plan by id",
            description = "Returns the treatment plan with the specified identifier, including its routines.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treatment plan retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Treatment plan not found")
    })
    public ResponseEntity<TreatmentPlanResource> getTreatmentPlanById(@PathVariable String id) {
        return treatmentPlanQueryService.handle(new GetTreatmentPlanByIdQuery(new TreatmentPlanId(UUID.fromString(id))))
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update treatment plan",
            description = "Updates the editable metadata of the treatment plan. This endpoint does not change the plan status.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated treatment plan lifecycle data.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = UpdateTreatmentPlanResource.class),
                    examples = @ExampleObject(
                            name = "Update treatment plan",
                            value = """
                                    {
                                      "name": "Forearm mobility plan - adjusted",
                                      "period": {
                                        "startsAt": "2026-06-01",
                                        "endsAt": "2026-06-21"
                                      }
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treatment plan updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Treatment plan not found")
    })
    public ResponseEntity<TreatmentPlanResource> updateTreatmentPlan(@PathVariable String id,
                                                                     @RequestBody UpdateTreatmentPlanResource resource) {
        var command = UpdateTreatmentPlanCommandFromResourceAssembler.toCommandFromResource(id, resource);
        return treatmentPlanCommandService.handle(command)
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate treatment plan",
            description = "Activates the specified treatment plan. Valid transition: SCHEDULED to ACTIVE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treatment plan activated successfully"),
            @ApiResponse(responseCode = "404", description = "Treatment plan not found"),
            @ApiResponse(responseCode = "409", description = "Transition is invalid or the plan conflicts with another scheduled or active plan")
    })
    public ResponseEntity<TreatmentPlanResource> activateTreatmentPlan(@PathVariable String id) {
        var command = new ActivateTreatmentPlanCommand(new TreatmentPlanId(UUID.fromString(id)));
        return treatmentPlanCommandService.handle(command)
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete treatment plan",
            description = "Completes the specified treatment plan. Valid transition: ACTIVE to COMPLETED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treatment plan completed successfully"),
            @ApiResponse(responseCode = "404", description = "Treatment plan not found"),
            @ApiResponse(responseCode = "409", description = "Transition is invalid")
    })
    public ResponseEntity<TreatmentPlanResource> completeTreatmentPlan(@PathVariable String id) {
        var command = new CompleteTreatmentPlanCommand(new TreatmentPlanId(UUID.fromString(id)));
        return treatmentPlanCommandService.handle(command)
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel treatment plan",
            description = "Cancels the specified treatment plan. Valid transitions: SCHEDULED to CANCELED and ACTIVE to CANCELED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treatment plan canceled successfully"),
            @ApiResponse(responseCode = "404", description = "Treatment plan not found"),
            @ApiResponse(responseCode = "409", description = "Transition is invalid")
    })
    public ResponseEntity<TreatmentPlanResource> cancelTreatmentPlan(@PathVariable String id) {
        var command = new CancelTreatmentPlanCommand(new TreatmentPlanId(UUID.fromString(id)));
        return treatmentPlanCommandService.handle(command)
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete treatment plan",
            description = "Deletes the treatment plan with the specified identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Treatment plan deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid treatment plan identifier"),
            @ApiResponse(responseCode = "404", description = "Treatment plan not found")
    })
    public ResponseEntity<Void> removeTreatmentPlan(@PathVariable String id) {
        var command = new RemoveTreatmentPlanCommand(new TreatmentPlanId(UUID.fromString(id)));
        treatmentPlanCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{treatmentPlanId}/routines")
    @Operation(summary = "Add routine to treatment plan",
            description = "Creates a new routine inside the specified treatment plan.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Routine data to add to the treatment plan.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CreateRoutineResource.class),
                    examples = @ExampleObject(
                            name = "Add routine",
                            value = """
                                    {
                                      "name": "Wednesday mobility",
                                      "order": 2,
                                      "schedule": {
                                        "dayOfWeek": "WEDNESDAY",
                                        "scheduledTime": "08:00:00"
                                      },
                                      "exerciseSeries": [
                                        {
                                          "order": 1,
                                          "exerciseId": "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c",
                                          "rangeOfMotionDegrees": 70,
                                          "repetitions": 10,
                                          "durationSeconds": 50,
                                          "restDurationSeconds": 25
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Treatment plan not found")
    })
    public ResponseEntity<TreatmentPlanResource> createRoutine(@PathVariable String treatmentPlanId,
                                                               @RequestBody CreateRoutineResource resource) {
        var command = CreateRoutineCommandFromResourceAssembler.toCommandFromResource(treatmentPlanId, resource);
        return treatmentPlanCommandService.handle(command)
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{treatmentPlanId}/routines/{routineOrder}")
    @Operation(summary = "Update routine",
            description = "Updates an existing routine inside the specified treatment plan.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated routine data.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = UpdateRoutineResource.class),
                    examples = @ExampleObject(
                            name = "Update routine",
                            value = """
                                    {
                                      "name": "Wednesday mobility progression",
                                      "newOrder": 2,
                                      "schedule": {
                                        "dayOfWeek": "WEDNESDAY",
                                        "scheduledTime": "09:00:00"
                                      },
                                      "exerciseSeries": [
                                        {
                                          "order": 1,
                                          "exerciseId": "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c",
                                          "rangeOfMotionDegrees": 80,
                                          "repetitions": 12,
                                          "durationSeconds": 55,
                                          "restDurationSeconds": 30
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Treatment plan or routine not found")
    })
    public ResponseEntity<TreatmentPlanResource> updateRoutine(@PathVariable String treatmentPlanId,
                                                               @PathVariable Integer routineOrder,
                                                               @RequestBody UpdateRoutineResource resource) {
        var command = UpdateRoutineCommandFromResourceAssembler.toCommandFromResource(treatmentPlanId, routineOrder, resource);
        return treatmentPlanCommandService.handle(command)
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{treatmentPlanId}/routines/{routineOrder}")
    @Operation(summary = "Delete routine",
            description = "Deletes the routine with the specified order from the treatment plan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid identifiers"),
            @ApiResponse(responseCode = "404", description = "Treatment plan or routine not found")
    })
    public ResponseEntity<TreatmentPlanResource> removeRoutine(@PathVariable String treatmentPlanId,
                                                               @PathVariable Integer routineOrder) {
        var command = new RemoveRoutineCommand(
                new TreatmentPlanId(UUID.fromString(treatmentPlanId)),
                new RoutineOrder(routineOrder));
        return treatmentPlanCommandService.handle(command)
                .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
