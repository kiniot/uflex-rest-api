package com.kiniot.uflex.api.planning.domain.model.aggregates;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDescription;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.MovementType;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExerciseTests {

    @Test
    void createExerciseStoresEnumsAndVideoAssetId() {
        var videoAssetId = UUID.randomUUID();
        var command = new CreateExerciseCommand(
                new ExerciseName("Wrist supination"),
                new ExerciseDescription("Controlled wrist supination exercise."),
                BodyPart.WRIST,
                MovementType.SUPINATION,
                videoAssetId);

        var exercise = new Exercise(command, new ClinicId(UUID.randomUUID()));

        assertEquals(BodyPart.WRIST, exercise.getBodyPart());
        assertEquals(MovementType.SUPINATION, exercise.getMovementType());
        assertEquals(videoAssetId, exercise.getVideoAssetId());
    }

    @Test
    void updateExerciseAllowsClearingVideoAssetId() {
        var createCommand = new CreateExerciseCommand(
                new ExerciseName("Elbow flexion"),
                new ExerciseDescription("Initial elbow flexion exercise."),
                BodyPart.ELBOW,
                MovementType.FLEXION,
                UUID.randomUUID());
        var exercise = new Exercise(createCommand, new ClinicId(UUID.randomUUID()));

        var updateCommand = new UpdateExerciseCommand(
                new ExerciseId(UUID.randomUUID()),
                new ExerciseName("Elbow flexion progression"),
                new ExerciseDescription("Updated elbow flexion exercise."),
                BodyPart.ELBOW,
                MovementType.FLEXION,
                null);

        exercise.update(updateCommand);

        assertEquals(BodyPart.ELBOW, exercise.getBodyPart());
        assertEquals(MovementType.FLEXION, exercise.getMovementType());
        assertNull(exercise.getVideoAssetId());
    }
}
