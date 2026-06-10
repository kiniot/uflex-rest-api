package com.kiniot.uflex.api.planning.interfaces.acl;

import com.kiniot.uflex.api.planning.interfaces.acl.dto.RoutineDetailsDto;

public interface PlanningContextFacade {
    boolean existsTreatmentPlanByPatientId(String patientId);

    RoutineDetailsDto getRoutineDetails(String routineId);

    boolean isRoutineInPatientTreatmentPlan(String routineId, String treatmentPlanId, String patientId);

    void onTherapySessionCompleted(String sessionId, String patientId, String finalizedAt);
}
