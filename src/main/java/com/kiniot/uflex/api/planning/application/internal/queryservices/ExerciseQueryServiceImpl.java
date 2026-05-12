package com.kiniot.uflex.api.planning.application.internal.queryservices;

import com.kiniot.uflex.api.planning.domain.model.entities.Exercise;
import com.kiniot.uflex.api.planning.domain.model.queries.GetAllExercisesQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetExerciseByIdQuery;
import com.kiniot.uflex.api.planning.domain.services.ExerciseQueryService;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.ExerciseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExerciseQueryServiceImpl implements ExerciseQueryService {

    private final ExerciseRepository exerciseRepository;

    public ExerciseQueryServiceImpl(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public Optional<Exercise> handle(GetExerciseByIdQuery query) {
        return exerciseRepository.findById(query.exerciseId());
    }

    @Override
    public List<Exercise> handle(GetAllExercisesQuery query) {
        return exerciseRepository.findAll();
    }
}
