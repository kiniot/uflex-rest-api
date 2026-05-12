package com.kiniot.uflex.api.planning.domain.services;

import com.kiniot.uflex.api.planning.domain.model.entities.Exercise;
import com.kiniot.uflex.api.planning.domain.model.queries.GetAllExercisesQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetExerciseByIdQuery;

import java.util.List;
import java.util.Optional;

public interface ExerciseQueryService {
    Optional<Exercise> handle(GetExerciseByIdQuery query);
    List<Exercise> handle(GetAllExercisesQuery query);
}
