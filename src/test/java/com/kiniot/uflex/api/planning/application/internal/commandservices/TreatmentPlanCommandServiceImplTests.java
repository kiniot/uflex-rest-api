package com.kiniot.uflex.api.planning.application.internal.commandservices;

import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseClinicMismatchException;
import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanRoutineCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDuration;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseSeries;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseSeriesOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RangeOfMotion;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RepetitionCount;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RestDuration;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineSchedule;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanPeriod;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanStatus;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.ExerciseRepository;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.RoutineRepository;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.TreatmentPlanRepository;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TreatmentPlanCommandServiceImplTests {

    private TreatmentPlanRepository treatmentPlanRepository;
    private ExerciseRepository exerciseRepository;
    private ExternalIamService externalIamService;
    private ExternalOrganizationService externalOrganizationService;
    private TreatmentPlanCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        treatmentPlanRepository = mock(TreatmentPlanRepository.class);
        var routineRepository = mock(RoutineRepository.class);
        exerciseRepository = mock(ExerciseRepository.class);
        externalIamService = mock(ExternalIamService.class);
        externalOrganizationService = mock(ExternalOrganizationService.class);
        service = new TreatmentPlanCommandServiceImpl(
                treatmentPlanRepository,
                routineRepository,
                exerciseRepository,
                externalIamService,
                externalOrganizationService
        );
    }

    @Test
    void createPlanRejectsRequestWithoutAuthenticatedClinic() {
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.empty());

        assertThrows(AuthenticatedUserClinicNotFoundException.class, () -> service.handle(createCommand()));
    }

    @Test
    void createPlanRejectsExerciseFromAnotherClinic() {
        var command = createCommand();
        var clinicId = new ClinicId(UUID.randomUUID());
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.of(clinicId));
        when(exerciseRepository.findByIdAndClinicId(any(), any())).thenReturn(Optional.empty());

        assertThrows(ExerciseClinicMismatchException.class, () -> service.handle(command));
        verify(externalOrganizationService).validatePatientBelongsToClinic(command.patientId(), clinicId);
    }

    @Test
    void createPlanValidatesTenantScopeAndPersistsScheduledPlan() {
        var command = createCommand();
        var clinicId = new ClinicId(UUID.randomUUID());
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.of(clinicId));
        when(exerciseRepository.findByIdAndClinicId(any(), any()))
                .thenReturn(Optional.of(mock(com.kiniot.uflex.api.planning.domain.model.aggregates.Exercise.class)));
        when(treatmentPlanRepository.findAllByClinicIdAndPatientId(clinicId, command.patientId()))
                .thenReturn(List.of());
        when(treatmentPlanRepository.save(any(TreatmentPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(command).orElseThrow();

        assertEquals(TreatmentPlanStatus.SCHEDULED, result.getStatus());
        assertEquals(clinicId, result.getClinicId());
        verify(externalOrganizationService).validatePatientBelongsToClinic(command.patientId(), clinicId);
        verify(treatmentPlanRepository).save(any(TreatmentPlan.class));
    }

    private CreateTreatmentPlanCommand createCommand() {
        return new CreateTreatmentPlanCommand(
                new PatientId(),
                new PlanName("Forearm protocol"),
                new TreatmentPlanPeriod(LocalDate.now().plusDays(1), LocalDate.now().plusDays(14)),
                List.of(new CreateTreatmentPlanRoutineCommand(
                        new RoutineName("Morning routine"),
                        new RoutineOrder(1),
                        new RoutineSchedule(DayOfWeek.MONDAY, LocalTime.of(8, 0)),
                        List.of(new ExerciseSeries(
                                new ExerciseSeriesOrder(1),
                                new ExerciseId(),
                                new RangeOfMotion(60),
                                new RepetitionCount(12),
                                new ExerciseDuration(45),
                                new RestDuration(20)
                        ))
                ))
        );
    }
}
