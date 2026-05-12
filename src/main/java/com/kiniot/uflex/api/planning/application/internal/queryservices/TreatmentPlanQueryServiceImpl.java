package com.kiniot.uflex.api.planning.application.internal.queryservices;

import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.queries.GetAllTreatmentPlansQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlanByIdQuery;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanQueryService;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.TreatmentPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TreatmentPlanQueryServiceImpl implements TreatmentPlanQueryService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final ExternalIamService externalIamService;

    public TreatmentPlanQueryServiceImpl(
            TreatmentPlanRepository treatmentPlanRepository,
            ExternalIamService externalIamService
    ) {
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    public Optional<TreatmentPlan> handle(GetTreatmentPlanByIdQuery query) {
        return treatmentPlanRepository.findWithRoutinesAndExerciseSeriesById(query.treatmentPlanId());
    }

    @Override
    public List<TreatmentPlan> handle(GetAllTreatmentPlansQuery query) {
        var clinicId = externalIamService.fetchCurrentAcademyId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return treatmentPlanRepository.findAllWithRoutinesAndExerciseSeriesByClinicId(clinicId);
    }
}
