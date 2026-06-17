package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateExerciseResource(
        @Schema(description = "Exercise display name", example = "Wrist supination")
        String name,
        @Schema(description = "Therapeutic description of the exercise", example = "Controlled wrist supination exercise focused on forearm rotation.")
        String description,
        @Schema(description = "Body part targeted by the exercise", allowableValues = {"ELBOW", "WRIST"}, example = "WRIST")
        String bodyPart,
        @Schema(description = "Movement type that defines the exercise", allowableValues = {"PRONATION", "SUPINATION", "FLEXION", "EXTENSION"}, example = "SUPINATION")
        String movementType,
        @Schema(description = "Optional public URL for the exercise video", example = "https://cdn.uflex.app/exercises/wrist-supination.mp4", nullable = true)
        String videoUrl
) {
}
