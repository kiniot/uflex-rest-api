package com.kiniot.uflex.api.planning.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record ExerciseResource(
        @Schema(example = "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c")
        String id,
        @Schema(example = "Wrist supination")
        String name,
        @Schema(example = "Controlled wrist supination exercise focused on forearm rotation.")
        String description,
        @Schema(allowableValues = {"ELBOW", "WRIST"}, example = "WRIST")
        String bodyPart,
        @Schema(allowableValues = {"PRONATION", "SUPINATION", "FLEXION", "EXTENSION"}, example = "SUPINATION")
        String movementType,
        @Schema(example = "https://cdn.uflex.app/exercises/wrist-supination.mp4", nullable = true)
        String videoUrl
) {
}
