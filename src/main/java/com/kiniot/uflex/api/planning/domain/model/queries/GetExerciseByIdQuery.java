package com.kiniot.uflex.api.planning.domain.model.queries;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;

public record GetExerciseByIdQuery(ExerciseId exerciseId) {
    public GetExerciseByIdQuery {
        if (exerciseId == null) {
            throw new IllegalArgumentException("Exercise ID cannot be null");
        }
    }
}
