package com.kiniot.uflex.api.organization.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.planning.interfaces.acl.PlanningContextFacade;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.springframework.stereotype.Service;

@Service
public class ExternalPlanningService {

    private final PlanningContextFacade planningContextFacade;

    public ExternalPlanningService(PlanningContextFacade planningContextFacade) {
        this.planningContextFacade = planningContextFacade;
    }

    public boolean existsTreatmentPlanByPatientId(PatientId patientId) {
        return planningContextFacade.existsTreatmentPlanByPatientId(patientId.patientId().toString());
    }
}
