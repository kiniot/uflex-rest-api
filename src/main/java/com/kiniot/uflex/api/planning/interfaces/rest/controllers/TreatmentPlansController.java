package com.kiniot.uflex.api.planning.interfaces.rest.controllers;

import com.kiniot.uflex.api.planning.domain.exceptions.TreatmentPlanWithIdNotFoundException;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.queries.GetAllTreatmentPlansQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlanByIdQuery;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanCommandService;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanQueryService;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateRoutineResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateTreatmentPlanResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.TreatmentPlanResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.UpdateRoutineResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.UpdateTreatmentPlanResource;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.CreateRoutineCommandFromResourceAssembler;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.CreateTreatmentPlanCommandFromResourceAssembler;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.TreatmentPlanResourceFromEntityAssembler;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.UpdateRoutineCommandFromResourceAssembler;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.UpdateTreatmentPlanCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.server.ResponseStatusException;

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
            description = "Returns all treatment plans associated with the authenticated user's clinic.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treatment plans retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or authenticated user has no clinic")
    })
    public ResponseEntity<List<TreatmentPlanResource>> getTreatmentPlans() {
        var treatmentPlans = treatmentPlanQueryService.handle(new GetAllTreatmentPlansQuery()).stream()
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

    @PostMapping
    @Operation(summary = "Create treatment plan",
            description = "Creates a new treatment plan for the authenticated user's clinic.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Treatment plan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or authenticated user has no clinic")
    })
    public ResponseEntity<TreatmentPlanResource> createTreatmentPlan(@RequestBody CreateTreatmentPlanResource resource) {
        try {
            var command = CreateTreatmentPlanCommandFromResourceAssembler.toCommandFromResource(resource);
            return treatmentPlanCommandService.handle(command)
                    .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                    .map(treatmentPlan -> new ResponseEntity<>(treatmentPlan, HttpStatus.CREATED))
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update treatment plan",
            description = "Updates the treatment plan with the specified identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treatment plan updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Treatment plan not found")
    })
    public ResponseEntity<TreatmentPlanResource> updateTreatmentPlan(@PathVariable String id,
                                                                     @RequestBody UpdateTreatmentPlanResource resource) {
        try {
            var command = UpdateTreatmentPlanCommandFromResourceAssembler.toCommandFromResource(id, resource);
            return treatmentPlanCommandService.handle(command)
                    .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (TreatmentPlanWithIdNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
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
        try {
            var command = new RemoveTreatmentPlanCommand(new TreatmentPlanId(UUID.fromString(id)));
            treatmentPlanCommandService.handle(command);
            return ResponseEntity.noContent().build();
        } catch (TreatmentPlanWithIdNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PostMapping("/routines")
    @Operation(summary = "Add routine to treatment plan",
            description = "Creates a new routine inside the specified treatment plan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Treatment plan not found")
    })
    public ResponseEntity<TreatmentPlanResource> createRoutine(@RequestBody CreateRoutineResource resource) {
        try {
            var command = CreateRoutineCommandFromResourceAssembler.toCommandFromResource(resource);
            return treatmentPlanCommandService.handle(command)
                    .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (TreatmentPlanWithIdNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PutMapping("/routines")
    @Operation(summary = "Update routine",
            description = "Updates an existing routine inside the specified treatment plan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Treatment plan or routine not found")
    })
    public ResponseEntity<TreatmentPlanResource> updateRoutine(@RequestBody UpdateRoutineResource resource) {
        try {
            var command = UpdateRoutineCommandFromResourceAssembler.toCommandFromResource(resource);
            return treatmentPlanCommandService.handle(command)
                    .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (TreatmentPlanWithIdNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
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
        try {
            var command = new RemoveRoutineCommand(
                    new TreatmentPlanId(UUID.fromString(treatmentPlanId)),
                    new RoutineOrder(routineOrder));
            return treatmentPlanCommandService.handle(command)
                    .map(TreatmentPlanResourceFromEntityAssembler::toResourceFromEntity)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (TreatmentPlanWithIdNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }
}
