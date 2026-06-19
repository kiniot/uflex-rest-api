package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.MovementType;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.CreateExerciseResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateExerciseCommandFromResourceAssemblerTests {

    @Test
    void toCommandFromResourceParsesEnumsAndOptionalVideoAssetId() {
        var resource = new CreateExerciseResource(
                "Wrist pronation",
                "Controlled wrist pronation exercise.",
                "WRIST",
                "PRONATION",
                null);

        var command = CreateExerciseCommandFromResourceAssembler.toCommandFromResource(resource);

        assertEquals(BodyPart.WRIST, command.bodyPart());
        assertEquals(MovementType.PRONATION, command.movementType());
        assertEquals(null, command.videoAssetId());
    }

    @Test
    void toCommandFromResourceRejectsInvalidBodyPart() {
        var resource = new CreateExerciseResource(
                "Exercise",
                "Description",
                "SHOULDER",
                "FLEXION",
                null);

        assertThrows(IllegalArgumentException.class, () -> CreateExerciseCommandFromResourceAssembler.toCommandFromResource(resource));
    }

    @Test
    void toCommandFromResourceRejectsInvalidMovementType() {
        var resource = new CreateExerciseResource(
                "Exercise",
                "Description",
                "ELBOW",
                "ROTATION",
                null);

        assertThrows(IllegalArgumentException.class, () -> CreateExerciseCommandFromResourceAssembler.toCommandFromResource(resource));
    }
}
