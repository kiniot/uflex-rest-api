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

        @Schema(description = "Media asset id of the exercise video. Send current value to keep it, null to clear it, or a new asset id to replace it.", example = "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c", nullable = true)
        String videoAssetId
) {}
