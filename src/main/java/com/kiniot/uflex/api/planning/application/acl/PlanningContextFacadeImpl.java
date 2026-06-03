package com.kiniot.uflex.api.planning.application.acl;

import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.TreatmentPlanRepository;
import com.kiniot.uflex.api.planning.interfaces.acl.PlanningContextFacade;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlanningContextFacadeImpl implements PlanningContextFacade {

    private final TreatmentPlanRepository treatmentPlanRepository;

    public PlanningContextFacadeImpl(TreatmentPlanRepository treatmentPlanRepository) {
        this.treatmentPlanRepository = treatmentPlanRepository;
    }

    @Override
    public boolean existsTreatmentPlanByPatientId(String patientId) {
        return treatmentPlanRepository.existsByPatientId(new PatientId(UUID.fromString(patientId)));
    }
}
