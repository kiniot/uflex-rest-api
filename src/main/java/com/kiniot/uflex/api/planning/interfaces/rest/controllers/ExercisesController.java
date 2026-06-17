package com.kiniot.uflex.api.planning.interfaces.rest.controllers;

import com.kiniot.uflex.api.planning.domain.model.commands.RemoveExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.queries.GetAllExercisesQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetExerciseByIdQuery;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.services.ExerciseCommandService;
import com.kiniot.uflex.api.planning.domain.services.ExerciseQueryService;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateExerciseResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.ExerciseResource;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.UpdateExerciseResource;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.CreateExerciseCommandFromResourceAssembler;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.ExerciseResourceFromEntityAssembler;
import com.kiniot.uflex.api.planning.interfaces.rest.transform.UpdateExerciseCommandFromResourceAssembler;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/exercises", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Exercises", description = "Available exercise endpoints")
public class ExercisesController {

    private final ExerciseCommandService exerciseCommandService;
    private final ExerciseQueryService exerciseQueryService;

    public ExercisesController(ExerciseCommandService exerciseCommandService,
                               ExerciseQueryService exerciseQueryService) {
        this.exerciseCommandService = exerciseCommandService;
        this.exerciseQueryService = exerciseQueryService;
    }

    @GetMapping
    @Operation(summary = "Get exercises",
            description = "Returns all exercises from the exercise catalog.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully")
    })
    public ResponseEntity<List<ExerciseResource>> getExercises() {
        var exercises = exerciseQueryService.handle(new GetAllExercisesQuery()).stream()
                .map(ExerciseResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exercise by id",
            description = "Returns the exercise with the specified identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercise retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    public ResponseEntity<ExerciseResource> getExerciseById(@PathVariable String id) {
        return exerciseQueryService.handle(new GetExerciseByIdQuery(new ExerciseId(UUID.fromString(id))))
                .map(ExerciseResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create exercise",
            description = "Creates a new therapeutic exercise in the clinic exercise catalog.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Exercise data to register in the catalog.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CreateExerciseResource.class),
                    examples = @ExampleObject(
                            name = "Create exercise",
                            value = """
                                    {
                                      "name": "Wrist supination",
                                      "description": "Controlled wrist supination exercise focused on forearm rotation.",
                                      "bodyPart": "WRIST",
                                      "movementType": "SUPINATION",
                                      "videoUrl": "https://cdn.uflex.app/exercises/wrist-supination.mp4"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exercise created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ExerciseResource> createExercise(@RequestBody CreateExerciseResource resource) {
        var command = CreateExerciseCommandFromResourceAssembler.toCommandFromResource(resource);
        return exerciseCommandService.handle(command)
                .map(ExerciseResourceFromEntityAssembler::toResourceFromEntity)
                .map(exercise -> new ResponseEntity<>(exercise, HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update exercise",
            description = "Updates the exercise with the specified identifier.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated exercise data.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = UpdateExerciseResource.class),
                    examples = @ExampleObject(
                            name = "Update exercise",
                            value = """
                                    {
                                      "name": "Wrist supination progression",
                                      "description": "Updated wrist supination exercise focused on controlled rotation and endurance.",
                                      "bodyPart": "WRIST",
                                      "movementType": "SUPINATION",
                                      "videoUrl": "https://cdn.uflex.app/exercises/wrist-supination-v2.mp4"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercise updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    public ResponseEntity<ExerciseResource> updateExercise(@PathVariable String id,
                                                           @RequestBody UpdateExerciseResource resource) {
        var command = UpdateExerciseCommandFromResourceAssembler.toCommandFromResource(id, resource);
        return exerciseCommandService.handle(command)
                .map(ExerciseResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete exercise",
            description = "Deletes the exercise with the specified identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Exercise deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid exercise identifier"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    public ResponseEntity<Void> removeExercise(@PathVariable String id) {
        var command = new RemoveExerciseCommand(new ExerciseId(UUID.fromString(id)));
        exerciseCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
