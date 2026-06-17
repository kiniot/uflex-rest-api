package com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.planning.interfaces.acl.PlanningContextFacade;
import com.kiniot.uflex.api.planning.interfaces.acl.dto.DailyRoutineDto;
import com.kiniot.uflex.api.planning.interfaces.acl.dto.RoutineDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service("therapyExternalPlanningService")
public class ExternalPlanningService {

    private final PlanningContextFacade planningContextFacade;

    public ExternalPlanningService(PlanningContextFacade planningContextFacade) {
        this.planningContextFacade = planningContextFacade;
    }

    public RoutineDetailsDto getRoutineDetails(String routineId) {
        log.debug("Fetching routine details: routineId={}", routineId);
        RoutineDetailsDto dto = planningContextFacade.getRoutineDetails(routineId);
        log.debug("Fetched routine details: routineId={}, seriesCount={}", routineId, dto.series().size());
        return dto;
    }

    public boolean isRoutineInPatientTreatmentPlan(String routineId, String treatmentPlanId, String patientId) {
        log.debug("Validating routine ownership: routineId={}, treatmentPlanId={}, patientId={}",
                routineId, treatmentPlanId, patientId);
        return planningContextFacade.isRoutineInPatientTreatmentPlan(routineId, treatmentPlanId, patientId);
    }

    public void notifyTherapySessionCompleted(String sessionId, String patientId, String finalizedAt) {
        log.debug("Notifying planning context of completed session: sessionId={}", sessionId);
        planningContextFacade.onTherapySessionCompleted(sessionId, patientId, finalizedAt);
        log.debug("Planning context notified: sessionId={}", sessionId);
    }

    public DailyRoutineDto resolveRoutineForDate(String clinicId, String patientId, LocalDate date) {
        log.debug("Resolving scheduled routine: clinicId={}, patientId={}, date={}", clinicId, patientId, date);
        return planningContextFacade.resolveRoutineForDate(clinicId, patientId, date);
    }
}
