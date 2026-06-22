package com.kiniot.uflex.api.therapy.application.internal.queryservices;

import com.kiniot.uflex.api.planning.interfaces.acl.dto.DailyRoutineDto;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalPlanningService;
import com.kiniot.uflex.api.therapy.domain.exceptions.SerieNotFoundException;
import com.kiniot.uflex.api.therapy.domain.exceptions.TherapySessionNotFoundException;
import com.kiniot.uflex.api.therapy.domain.exceptions.TherapySessionStillInProgressException;
import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.queries.*;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.TherapySessionId;
import com.kiniot.uflex.api.therapy.domain.services.TherapySessionQueryService;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.TherapySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TherapySessionQueryServiceImpl implements TherapySessionQueryService {

    private final TherapySessionRepository therapySessionRepository;
    private final ExternalIamService externalIamService;
    private final ExternalPlanningService externalPlanningService;
    private final ExternalOrganizationService externalOrganizationService;

    @Override
    public TherapySession handle(GetTherapySessionByIdQuery query) {
        log.debug("Finding therapy session: sessionId={}", query.sessionId());
        TherapySession session = therapySessionRepository.findById(TherapySessionId.of(query.sessionId()))
                .orElseThrow(() -> TherapySessionNotFoundException.withId(query.sessionId().toString()));
        ensureSessionAccess(session);
        return session;
    }

    @Override
    public TherapySession handle(GetActiveTherapySessionByPatientIdQuery query) {
        log.debug("Finding active therapy session: patientId={}", query.patientId());
        ensurePatientAccess(query.patientId());
        ClinicId clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated clinic"));
        return therapySessionRepository.findActiveByPatientId(query.patientId(), clinicId.id(), SessionStatus.ACTIVE_STATUSES)
                .orElseThrow(() -> TherapySessionNotFoundException.withId(
                        "active session for patient " + query.patientId()));
    }

    @Override
    public TherapySession handle(GetActiveTherapySessionByDeviceSerialQuery query) {
        log.debug("Finding active therapy session: deviceSerial={}", query.deviceSerial());
        // Per-edge least-privilege: an edge caller may only query its own kit's serial.
        ensureEdgeMayQuerySerial(query.deviceSerial());
        // Naturally clinic-scoped: only sessions of the caller's clinic are returned, so the
        // device-serial lookup cannot leak another clinic's session.
        ClinicId clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated clinic"));
        return therapySessionRepository.findActiveByIotDeviceId(query.deviceSerial(), clinicId.id(), SessionStatus.ACTIVE_STATUSES)
                .orElseThrow(() -> TherapySessionNotFoundException.withId(
                        "active session for device " + query.deviceSerial()));
    }

    @Override
    public TherapySession handle(GetSessionProgressQuery query) {
        log.debug("Fetching session progress: sessionId={}", query.sessionId());
        TherapySession session = therapySessionRepository.findById(TherapySessionId.of(query.sessionId()))
                .orElseThrow(() -> TherapySessionNotFoundException.withId(query.sessionId().toString()));
        ensureSessionAccess(session);
        return session;
    }

    @Override
    public TherapySession handle(GetSessionSummaryQuery query) {
        log.debug("Fetching session summary: sessionId={}", query.sessionId());
        TherapySession session = therapySessionRepository.findById(TherapySessionId.of(query.sessionId()))
                .orElseThrow(() -> TherapySessionNotFoundException.withId(query.sessionId().toString()));
        ensureSessionAccess(session);

        if (session.getStatus() != SessionStatus.Completed && session.getStatus() != SessionStatus.Cancelled) {
            throw TherapySessionStillInProgressException.forSession(query.sessionId().toString());
        }
        return session;
    }

    @Override
    public Serie handle(GetSerieDetailsQuery query) {
        log.debug("Fetching serie details: sessionId={}, serieId={}", query.sessionId(), query.serieId());
        TherapySession session = therapySessionRepository.findById(TherapySessionId.of(query.sessionId()))
                .orElseThrow(() -> TherapySessionNotFoundException.withId(query.sessionId().toString()));
        ensureSessionAccess(session);

        SerieId serieId = SerieId.of(query.serieId());
        return session.findSerie(serieId)
                .orElseThrow(() -> SerieNotFoundException.withId(query.serieId().toString()));
    }

    @Override
    public DailyRoutineDto handle(GetDailyScheduleQuery query) {
        log.debug("Resolving daily schedule: patientId={}, date={}", query.patientId(), query.date());
        ensurePatientAccess(query.patientId());
        ClinicId clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated clinic"));
        return externalPlanningService.resolveRoutineForDate(
                clinicId.id().toString(), query.patientId().toString(), query.date());
    }

    private void ensureSessionAccess(TherapySession session) {
        if (currentHasAuthority("ROLE_PATIENT")) {
            ensureSessionBelongsToCurrentPatient(session);
            return;
        }
        ensureSessionBelongsToAuthenticatedClinic(session);
    }

    private void ensurePatientAccess(java.util.UUID patientId) {
        if (currentHasAuthority("ROLE_PATIENT")) {
            ensureRequestedPatientIsCurrentPatient(patientId);
            return;
        }
        ensurePatientBelongsToAuthenticatedClinic(patientId);
    }

    private void ensureSessionBelongsToAuthenticatedClinic(TherapySession session) {
        ClinicId clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated clinic"));
        if (!clinicId.equals(session.getClinicId())) {
            throw new AccessDeniedException("You do not have permission to access this therapy session");
        }
    }

    private void ensurePatientBelongsToAuthenticatedClinic(java.util.UUID patientId) {
        ClinicId clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated clinic"));
        boolean belongs = externalOrganizationService.patientBelongsToClinic(
                patientId.toString(),
                clinicId.id().toString()
        );
        if (!belongs) {
            throw new AccessDeniedException("You do not have permission to access this patient");
        }
    }

    private void ensureSessionBelongsToCurrentPatient(TherapySession session) {
        PatientId currentPatientId = externalOrganizationService.fetchCurrentPatientId()
                .orElseThrow(() -> new AccessDeniedException("Current patient profile not found"));
        if (!currentPatientId.equals(session.getPatientId())) {
            throw new AccessDeniedException("You do not have permission to access this therapy session");
        }
    }

    private void ensureRequestedPatientIsCurrentPatient(java.util.UUID patientId) {
        PatientId currentPatientId = externalOrganizationService.fetchCurrentPatientId()
                .orElseThrow(() -> new AccessDeniedException("Current patient profile not found"));
        if (!currentPatientId.equals(PatientId.of(patientId))) {
            throw new AccessDeniedException("You do not have permission to access this patient");
        }
    }

    /**
     * Per-edge least-privilege for the device-serial lookup: a {@code ROLE_EDGE} caller may only
     * query the serial of the kit it is bound to. Human callers (clinic admin / physiotherapist)
     * remain unrestricted here; their clinic scope is enforced by the query below.
     */
    private void ensureEdgeMayQuerySerial(String requestedSerial) {
        if (!currentHasAuthority("ROLE_EDGE")) {
            return;
        }
        String edgeSerial = externalIamService.findEdgeSerialForCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Edge service account is not bound to a kit"));
        if (!edgeSerial.equals(requestedSerial)) {
            throw new AccessDeniedException("An edge may only query its own kit");
        }
    }

    private boolean currentHasAuthority(String authority) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}
