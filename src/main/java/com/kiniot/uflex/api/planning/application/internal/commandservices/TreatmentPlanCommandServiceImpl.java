package com.kiniot.uflex.api.planning.application.internal.commandservices;

import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanCommandService;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.TreatmentPlanRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TreatmentPlanCommandServiceImpl implements TreatmentPlanCommandService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final ExternalIamService externalIamService;

    public TreatmentPlanCommandServiceImpl(
            TreatmentPlanRepository treatmentPlanRepository,
            ExternalIamService externalIamService
    ) {
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    public Optional<TreatmentPlan> handle(CreateTreatmentPlanCommand command) {
        var clinicId = externalIamService.fetchCurrentAcademyId()
                .orElseThrow(() -> new IllegalStateException("No clinic found"));
        var treatmentPlan = new TreatmentPlan(command, clinicId);
        try {
            treatmentPlanRepository.save(treatmentPlan);
            return Optional.of(treatmentPlanRepository.save(treatmentPlan));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create treatment plan: %s".formatted(e.getMessage()));
        }
    }
}
