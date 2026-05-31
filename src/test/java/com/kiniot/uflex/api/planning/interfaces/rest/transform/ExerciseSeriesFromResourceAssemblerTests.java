package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.interfaces.rest.resources.ExerciseSeriesRequestResource;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExerciseSeriesFromResourceAssemblerTests {

    @Test
    void toValueObjectFromResourceMapsSeriesWithoutMovementType() {
        var exerciseId = UUID.randomUUID().toString();
        var resource = new ExerciseSeriesRequestResource(1, exerciseId, 60, 12, 45, 20);

        var valueObject = ExerciseSeriesFromResourceAssembler.toValueObjectFromResource(resource);

        assertEquals(1, valueObject.order().value());
        assertEquals(exerciseId, valueObject.exerciseId().id().toString());
        assertEquals(60, valueObject.rangeOfMotion().degrees());
        assertEquals(12, valueObject.repetitions().value());
        assertEquals(45, valueObject.duration().seconds());
        assertEquals(20, valueObject.restDuration().seconds());
    }

    @Test
    void toValueObjectListFromResourceReturnsEmptyListForNullInput() {
        assertTrue(ExerciseSeriesFromResourceAssembler.toValueObjectListFromResource(null).isEmpty());
    }
}
