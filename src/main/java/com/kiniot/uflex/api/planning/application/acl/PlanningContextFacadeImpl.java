package com.kiniot.uflex.api.planning.application.acl;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.RoutineRepository;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.TreatmentPlanRepository;
import com.kiniot.uflex.api.planning.interfaces.acl.PlanningContextFacade;
import com.kiniot.uflex.api.planning.interfaces.acl.dto.RoutineDetailsDto;
import com.kiniot.uflex.api.planning.interfaces.acl.dto.SerieDetailsDto;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PlanningContextFacadeImpl implements PlanningContextFacade {

    private final RoutineRepository routineRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;

    public PlanningContextFacadeImpl(
            RoutineRepository routineRepository,
            TreatmentPlanRepository treatmentPlanRepository
    ) {
        this.routineRepository = routineRepository;
        this.treatmentPlanRepository = treatmentPlanRepository;
    }

    @Override
    public boolean existsTreatmentPlanByPatientId(String patientId) {
        return treatmentPlanRepository.existsByPatientId(new PatientId(UUID.fromString(patientId)));
    }

    @Override
    @Transactional(readOnly = true)
    public RoutineDetailsDto getRoutineDetails(String routineId) {
        var id = new RoutineId(UUID.fromString(routineId));
        var routine = routineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Routine not found: " + routineId));

        List<SerieDetailsDto> series = routine.getExerciseSeries().stream()
                .sorted(Comparator.comparing(exerciseSeries -> exerciseSeries.order().value()))
                .map(exerciseSeries -> new SerieDetailsDto(
                        exerciseSeries.exerciseId().id().toString(),
                        exerciseSeries.repetitions().value(),
                        exerciseSeries.rangeOfMotion().degrees().doubleValue(),
                        exerciseSeries.duration().seconds(),
                        exerciseSeries.restDuration().seconds()
                ))
                .toList();

        return new RoutineDetailsDto(routineId, series);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoutineInPatientTreatmentPlan(String routineId, String treatmentPlanId, String patientId) {
        return treatmentPlanRepository.existsByIdAndPatientIdAndRoutinesId(
                new TreatmentPlanId(UUID.fromString(treatmentPlanId)),
                new PatientId(UUID.fromString(patientId)),
                new RoutineId(UUID.fromString(routineId))
        );
    }

    @Override
    public void onTherapySessionCompleted(String sessionId, String patientId, String finalizedAt) {
        log.info("Therapy session completed notification received: sessionId={}, patientId={}, finalizedAt={}",
                sessionId, patientId, finalizedAt);
    }
}
