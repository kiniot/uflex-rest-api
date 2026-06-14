package com.kiniot.uflex.api.planning.interfaces.acl;

import com.kiniot.uflex.api.planning.interfaces.acl.dto.DailyRoutineDto;
import com.kiniot.uflex.api.planning.interfaces.acl.dto.RoutineDetailsDto;

import java.time.LocalDate;
import java.util.Optional;

public interface PlanningContextFacade {
    boolean existsTreatmentPlanByPatientId(String patientId);

    RoutineDetailsDto getRoutineDetails(String routineId);

    boolean isRoutineInPatientTreatmentPlan(String routineId, String treatmentPlanId, String patientId);

    void onTherapySessionCompleted(String sessionId, String patientId, String finalizedAt);

    /**
     * Resolves the routine scheduled for the patient on the given date within the
     * clinic. A routine matches when its active treatment plan covers the date and
     * its schedule falls on that day of week; when several match the same day, the
     * one with the earliest scheduled time wins. Returns empty when no active plan
     * covers the date or no routine is scheduled for that day.
     */
    Optional<DailyRoutineDto> resolveRoutineForDate(String clinicId, String patientId, LocalDate date);
}
