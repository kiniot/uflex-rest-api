package com.kiniot.uflex.api.planning.application.internal.queryservices;

import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.planning.domain.model.aggregates.Exercise;
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
    private final ExternalIamService externalIamService;

    public ExerciseQueryServiceImpl(ExerciseRepository exerciseRepository, ExternalIamService externalIamService) {
        this.exerciseRepository = exerciseRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    public Optional<Exercise> handle(GetExerciseByIdQuery query) {
        var clinicId = externalIamService.fetchCurrentAcademyId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return exerciseRepository.findByIdAndClinicId(query.exerciseId(), clinicId);
    }

    @Override
    public List<Exercise> handle(GetAllExercisesQuery query) {
        var clinicId = externalIamService.fetchCurrentAcademyId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return exerciseRepository.findAllByClinicId(clinicId);
    }
}
