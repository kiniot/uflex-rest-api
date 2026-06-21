package com.kiniot.uflex.api.therapy.application.internal.commandservices;

import com.kiniot.uflex.api.planning.interfaces.acl.dto.RoutineDetailsDto;
import com.kiniot.uflex.api.planning.interfaces.acl.dto.SerieDetailsDto;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalDeviceService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalPlanningService;
import com.kiniot.uflex.api.therapy.domain.exceptions.PatientAlreadyInActiveSessionException;
import com.kiniot.uflex.api.therapy.domain.exceptions.TherapySessionNotFoundException;
import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.commands.*;
import com.kiniot.uflex.api.therapy.domain.model.entities.AnomalousMovement;
import com.kiniot.uflex.api.therapy.domain.model.entities.RoutineExecution;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.*;
import com.kiniot.uflex.api.therapy.domain.services.TherapySessionCommandService;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.TherapySessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TherapySessionCommandServiceImpl implements TherapySessionCommandService {

    private final TherapySessionRepository therapySessionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ExternalPlanningService externalPlanningService;
    private final ExternalIamService externalIamService;
    private final ExternalDeviceService externalDeviceService;
    private final ExternalOrganizationService externalOrganizationService;

    @Override
    @Transactional
    public TherapySession handle(InitiateTherapyPreparationCommand command) {
        ClinicId clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated clinic"));
        ensurePatientBelongsToAuthenticatedClinic(command.patientId(), clinicId);

        if (therapySessionRepository.existsActiveByPatientId(command.patientId(), clinicId.id(), SessionStatus.ACTIVE_STATUSES)) {
            throw PatientAlreadyInActiveSessionException.forPatient(command.patientId().toString());
        }

        if (!externalDeviceService.isDeviceAssignedToPatient(
                command.iotDeviceId(), clinicId.id().toString(), command.patientId().toString())) {
            throw new IllegalArgumentException(
                    "Device %s is not assigned to patient %s in this clinic".formatted(command.iotDeviceId(), command.patientId()));
        }

        if (!externalPlanningService.isRoutineInPatientTreatmentPlan(
                command.planningRoutineId().toString(),
                command.treatmentPlanId().toString(),
                command.patientId().toString())) {
            throw new IllegalArgumentException(
                    "Routine %s does not belong to treatment plan %s of patient %s"
                            .formatted(command.planningRoutineId(), command.treatmentPlanId(), command.patientId()));
        }

        RoutineDetailsDto routineDto = externalPlanningService.getRoutineDetails(command.planningRoutineId().toString());

        List<Serie> series = routineDto.series().stream()
                .map(this::buildSerie)
                .toList();
        RoutineExecution routine = new RoutineExecution(command.planningRoutineId(), series);

        TherapySession session = new TherapySession(command, routine, clinicId);
        TherapySession saved = therapySessionRepository.save(session);

        eventPublisher.publishEvent(saved.publishTherapyPreparationInitiated());

        log.info("TherapySession initiated: sessionId={}, patientId={}, treatmentPlanId={}",
                TherapySessionId.toStringOrNull(saved.getId()),
                command.patientId(),
                command.treatmentPlanId());
        return saved;
    }

    @Override
    @Transactional
    public TherapySession handle(ConfirmHardwareReadinessCommand command) {
        TherapySession session = findOwnedSession(command.sessionId());

        session.confirmHardwareReadiness(command.sensorsPlaced());
        TherapySession saved = therapySessionRepository.save(session);

        eventPublisher.publishEvent(saved.publishHardwareReadinessConfirmed());

        log.info("Hardware readiness confirmed: sessionId={}", command.sessionId());
        return saved;
    }

    @Override
    @Transactional
    public TherapySession handle(StartTherapySessionCommand command) {
        TherapySession session = findOwnedSession(command.sessionId());

        session.startSession();
        TherapySession saved = therapySessionRepository.save(session);

        eventPublisher.publishEvent(saved.publishRoutineStarted());

        log.info("TherapySession started: sessionId={}", command.sessionId());
        return saved;
    }

    @Override
    @Transactional
    public TherapySession handle(FinalizeTherapySessionCommand command) {
        TherapySession session = findOwnedSession(command.sessionId());

        session.finalizeSession();
        TherapySession saved = therapySessionRepository.save(session);

        eventPublisher.publishEvent(saved.publishTherapySessionCompleted());

        log.info("TherapySession finalized: sessionId={}, patientId={}",
                command.sessionId(),
                PatientId.toStringOrNull(saved.getPatientId()));
        return saved;
    }

    @Override
    @Transactional
    public TherapySession handle(CancelTherapySessionCommand command) {
        TherapySession session = findOwnedSession(command.sessionId());

        session.cancelSession(command.reason());
        TherapySession saved = therapySessionRepository.save(session);

        eventPublisher.publishEvent(saved.publishTherapySessionCancelled());

        log.info("TherapySession cancelled: sessionId={}, reason={}", command.sessionId(), command.reason());
        return saved;
    }

    @Override
    @Transactional
    public TherapySession handle(StartSerieCommand command) {
        TherapySession session = findOwnedSession(command.sessionId());

        SerieId serieId = SerieId.of(command.serieId());
        session.startSerie(serieId);
        TherapySession saved = therapySessionRepository.save(session);

        eventPublisher.publishEvent(saved.publishSerieStarted(serieId));

        log.info("Serie started: sessionId={}, serieId={}", command.sessionId(), command.serieId());
        return saved;
    }

    @Override
    @Transactional
    public TherapySession handle(RecordValidRepetitionCommand command) {
        TherapySession session = findOwnedSession(command.sessionId());

        SerieId serieId = SerieId.of(command.serieId());
        boolean recorded = session.recordRepetition(
                serieId, command.achievedAngle(), command.recordedAt(), command.edgeSequenceId());
        TherapySession saved = therapySessionRepository.save(session);

        if (recorded) {
            eventPublisher.publishEvent(saved.publishRepetitionRecorded(serieId, command.achievedAngle(), command.recordedAt()));

            saved.findSerie(serieId).ifPresent(serie -> {
                if (serie.getStatus() == SerieStatus.Validated) {
                    eventPublisher.publishEvent(saved.publishSerieAchieved(serieId));
                }
            });
        }

        log.info("Repetition recorded: sessionId={}, serieId={}, achievedAngle={}",
                command.sessionId(), command.serieId(), command.achievedAngle());
        return saved;
    }

    @Override
    @Transactional
    public void handle(RecordAnomalousMovementCommand command) {
        TherapySession session = findOwnedSession(command.sessionId());

        AlertType alertType = AlertType.of(command.alertType());
        AnomalousMovement anomaly = session.recordAnomalousMovement(alertType);
        TherapySession saved = therapySessionRepository.save(session);

        if (alertType == AlertType.ExcessiveMovement) {
            eventPublisher.publishEvent(saved.publishExcessiveMovementAlertIssued(anomaly));
        } else {
            eventPublisher.publishEvent(saved.publishAnomalousMovementDetected(anomaly));
        }

        log.info("Anomalous movement recorded: sessionId={}, alertType={}", command.sessionId(), command.alertType());
    }

    @Override
    @Transactional
    public void handle(ReportPainLevelCommand command) {
        TherapySession session = findOwnedSession(command.sessionId());

        session.reportPainLevel(PainLevel.of(command.painLevel()));
        TherapySession saved = therapySessionRepository.save(session);

        eventPublisher.publishEvent(saved.publishPainLevelReported());

        log.info("Pain level reported: sessionId={}, painLevel={}", command.sessionId(), command.painLevel());
    }

    private Serie buildSerie(SerieDetailsDto dto) {
        // Therapy defines the valid range as [0, rangeOfMotion]: planning only
        // prescribes the target range of motion, not a lower bound.
        return new Serie(
                ExerciseId.of(java.util.UUID.fromString(dto.exerciseId())),
                dto.targetRepetitions(),
                AngleThreshold.of(0.0, dto.rangeOfMotion()),
                dto.durationSeconds(),
                dto.restDurationSeconds()
        );
    }

    private TherapySession findOwnedSession(java.util.UUID sessionId) {
        TherapySession session = therapySessionRepository.findById(TherapySessionId.of(sessionId))
                .orElseThrow(() -> TherapySessionNotFoundException.withId(sessionId.toString()));
        ensureSessionAccess(session);
        return session;
    }

    private void ensureSessionAccess(TherapySession session) {
        if (currentHasAuthority("ROLE_PATIENT")) {
            ensureSessionBelongsToCurrentPatient(session);
            return;
        }
        ensureSessionBelongsToAuthenticatedClinic(session);
    }

    private void ensureSessionBelongsToAuthenticatedClinic(TherapySession session) {
        ClinicId clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated clinic"));
        if (!clinicId.equals(session.getClinicId())) {
            throw new AccessDeniedException("You do not have permission to access this therapy session");
        }
    }

    private void ensureSessionBelongsToCurrentPatient(TherapySession session) {
        PatientId currentPatientId = externalOrganizationService.fetchCurrentPatientId()
                .orElseThrow(() -> new AccessDeniedException("Current patient profile not found"));
        if (!currentPatientId.equals(session.getPatientId())) {
            throw new AccessDeniedException("You do not have permission to access this therapy session");
        }
    }

    private void ensurePatientBelongsToAuthenticatedClinic(java.util.UUID patientId, ClinicId clinicId) {
        boolean belongs = externalOrganizationService.patientBelongsToClinic(
                patientId.toString(),
                clinicId.id().toString()
        );
        if (!belongs) {
            throw new AccessDeniedException("You do not have permission to access this patient");
        }
    }

    private boolean currentHasAuthority(String authority) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}
