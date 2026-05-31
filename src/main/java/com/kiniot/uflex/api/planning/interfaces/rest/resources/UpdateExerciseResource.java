package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateExerciseResource(
        @Schema(description = "Exercise display name", example = "Wrist supination progression")
        String name,
        @Schema(description = "Therapeutic description of the exercise", example = "Updated wrist supination exercise focused on controlled rotation and endurance.")
        String description,
        @Schema(description = "Body part targeted by the exercise", allowableValues = {"ELBOW", "WRIST"}, example = "WRIST")
        String bodyPart,
        @Schema(description = "Movement type that defines the exercise", allowableValues = {"PRONATION", "SUPINATION", "FLEXION", "EXTENSION"}, example = "SUPINATION")
        String movementType,
        @Schema(description = "Optional public URL for the exercise video", example = "https://cdn.uflex.app/exercises/wrist-supination-v2.mp4", nullable = true)
        String videoUrl
) {
}
