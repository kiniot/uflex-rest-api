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
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.KitSerial;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.RepetitionClassification;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieStatus;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.TherapySessionId;
import com.kiniot.uflex.api.therapy.domain.services.TherapySessionQueryService;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.TherapySessionRepository;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.CompensatoryMovementCountRow;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.PatientTherapyOverviewRow;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.SerieRepetitionAggregateRow;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.TherapySessionHistoryRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TherapySessionQueryServiceImpl implements TherapySessionQueryService {

    /**
     * Hard cap on the history read. The clinic web plots the whole result rather than a page, so a
     * server-side page size would silently truncate the trend line; this bound only exists to keep a
     * pathological patient from returning unbounded rows.
     */
    private static final int HISTORY_MAX_RESULTS = 500;

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
    public List<TherapySessionHistoryItem> handle(GetTherapySessionHistoryQuery query) {
        log.debug("Listing therapy session history: patientId={}, treatmentPlanId={}",
                query.patientId(), query.treatmentPlanId());
        ensurePatientAccess(query.patientId());
        ClinicId clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated clinic"));

        List<TherapySessionHistoryRow> rows = query.treatmentPlanId() != null
                ? therapySessionRepository.findHistoryByPatientIdAndTreatmentPlanId(
                        query.patientId(), clinicId.id(), query.treatmentPlanId(),
                        Limit.of(HISTORY_MAX_RESULTS))
                : therapySessionRepository.findHistoryByPatientId(
                        query.patientId(), clinicId.id(), Limit.of(HISTORY_MAX_RESULTS));
        if (rows.isEmpty()) {
            return List.of();
        }

        List<UUID> sessionIds = rows.stream().map(TherapySessionHistoryRow::sessionId).toList();
        Map<UUID, SerieRepetitionAggregateRow> executionAggregates = therapySessionRepository
                .aggregateSeriesAndRepetitions(sessionIds, SerieStatus.Completed,
                        RepetitionClassification.Good, RepetitionClassification.Incomplete,
                        RepetitionClassification.Unsafe)
                .stream()
                .collect(Collectors.toMap(SerieRepetitionAggregateRow::sessionId, row -> row));
        Map<UUID, Long> compensatoryCounts = therapySessionRepository
                .countCompensatoryMovements(sessionIds)
                .stream()
                .collect(Collectors.toMap(CompensatoryMovementCountRow::sessionId,
                        CompensatoryMovementCountRow::compensatoryMovementsDetected));

        return rows.stream()
                .map(row -> toHistoryItem(row, executionAggregates.get(row.sessionId()),
                        compensatoryCounts.get(row.sessionId())))
                .toList();
    }

    @Override
    public List<PatientTherapyOverview> handle(GetPatientTherapyOverviewQuery query) {
        log.debug("Listing patient therapy overview for the current physiotherapist");
        String physiotherapistId = externalOrganizationService.fetchCurrentPhysiotherapistId()
                .orElseThrow(() -> new AccessDeniedException("Current physiotherapist profile not found"));
        ClinicId clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated clinic"));

        List<PatientId> patientIds = externalOrganizationService.findPatientIdsByPhysiotherapistAndClinic(
                physiotherapistId, clinicId.id().toString());
        if (patientIds.isEmpty()) {
            return List.of();
        }

        List<UUID> rawPatientIds = patientIds.stream().map(PatientId::id).toList();
        Map<UUID, PatientTherapyOverviewRow> aggregates = therapySessionRepository
                .aggregateTherapyOverviewByPatient(rawPatientIds, clinicId.id(),
                        SessionStatus.Completed, RepetitionClassification.Good)
                .stream()
                .collect(Collectors.toMap(PatientTherapyOverviewRow::patientId, row -> row));
        Set<UUID> patientsInSession = Set.copyOf(therapySessionRepository.findPatientIdsWithActiveSession(
                rawPatientIds, clinicId.id(), SessionStatus.ACTIVE_STATUSES));
        Map<String, String> names = externalOrganizationService.fetchPatientNames(patientIds);

        return rawPatientIds.stream()
                .map(patientId -> toOverview(patientId, names.get(patientId.toString()),
                        aggregates.get(patientId), patientsInSession.contains(patientId)))
                // Never-started patients sort first: an untouched caseload is the point of this list.
                .sorted(Comparator.comparing(PatientTherapyOverview::lastSessionAt,
                        Comparator.nullsFirst(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public TherapySession handle(GetTherapySessionDetailQuery query) {
        log.debug("Fetching session detail: sessionId={}", query.sessionId());
        TherapySession session = therapySessionRepository.findById(TherapySessionId.of(query.sessionId()))
                .orElseThrow(() -> TherapySessionNotFoundException.withId(query.sessionId().toString()));
        ensureSessionAccess(session);
        // Pull series + repetitions into the persistence context so the assembler can walk the graph
        // once this read-only transaction ends. Entity identity makes the Serie instances hanging off
        // routine.series the very ones loaded here, repetitions included.
        therapySessionRepository.findSeriesWithRepetitionsBySessionId(query.sessionId());
        // No StillInProgress guard on purpose: unlike the summary, the detail must answer for a
        // running session so the clinician can follow it live.
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

    @Override
    public EdgeConnection handle(GetEdgeConnectionForCurrentPatientQuery query) {
        log.debug("Resolving edge connection for current patient");
        PatientId patientId = externalOrganizationService.fetchCurrentPatientId()
                .orElseThrow(() -> new AccessDeniedException("Current patient profile not found"));
        ClinicId clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated clinic"));
        TherapySession session = therapySessionRepository
                .findActiveByPatientId(patientId.id(), clinicId.id(), SessionStatus.ACTIVE_STATUSES)
                .orElseThrow(() -> TherapySessionNotFoundException.withId(
                        "active session for patient " + patientId.id()));
        String serial = KitSerial.toStringOrNull(session.getIotDeviceId());
        String lanUrl = externalIamService.findEdgeLanUrlBySerial(serial).orElse(null);
        return new EdgeConnection(lanUrl, session.getEdgePairingToken(), null);
    }

    /** A patient with no sessions at all yields no aggregate row, hence the zeroed defaults. */
    private PatientTherapyOverview toOverview(UUID patientId, String fullName,
                                              PatientTherapyOverviewRow row, boolean hasActiveSession) {
        return new PatientTherapyOverview(
                patientId,
                fullName,
                row != null ? Math.toIntExact(row.totalSessions()) : 0,
                row != null ? Math.toIntExact(row.completedSessions()) : 0,
                row != null ? Math.toIntExact(row.sessionsRequiringReview()) : 0,
                row != null ? row.lastSessionAt() : null,
                row != null ? Math.toIntExact(row.totalRepetitions()) : 0,
                row != null ? Math.toIntExact(row.goodRepetitions()) : 0,
                row != null ? row.averageAchievedRom() : null,
                hasActiveSession
        );
    }

    /**
     * Stitches the three history queries by session id. A session with no series yields no row in
     * the aggregate queries, hence the null-tolerant defaults.
     */
    private TherapySessionHistoryItem toHistoryItem(TherapySessionHistoryRow row,
                                                    SerieRepetitionAggregateRow aggregates,
                                                    Long compensatoryMovements) {
        return new TherapySessionHistoryItem(
                row.sessionId(),
                row.status(),
                row.startedAt(),
                row.finalizedAt(),
                row.treatmentPlanId(),
                row.planningRoutineId(),
                aggregates != null ? Math.toIntExact(aggregates.totalSeries()) : 0,
                aggregates != null ? Math.toIntExact(aggregates.completedSeries()) : 0,
                aggregates != null ? Math.toIntExact(aggregates.totalRepetitions()) : 0,
                aggregates != null ? Math.toIntExact(aggregates.goodRepetitions()) : 0,
                aggregates != null ? Math.toIntExact(aggregates.incompleteRepetitions()) : 0,
                aggregates != null ? Math.toIntExact(aggregates.unsafeRepetitions()) : 0,
                aggregates != null ? aggregates.averageAchievedRom() : null,
                row.painLevel(),
                row.maxReportedPainLevel(),
                row.requiresClinicalReview(),
                compensatoryMovements != null ? Math.toIntExact(compensatoryMovements) : 0
        );
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
