package com.kiniot.uflex.api.therapy.application.internal.queryservices;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalIamService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TherapySessionQueryServiceImpl implements TherapySessionQueryService {

    private static final List<SessionStatus> ACTIVE_STATUSES =
            List.of(SessionStatus.Pending, SessionStatus.Ready, SessionStatus.InProgress);

    private final TherapySessionRepository therapySessionRepository;
    private final ExternalIamService externalIamService;

    @Override
    public TherapySession handle(GetTherapySessionByIdQuery query) {
        log.debug("Finding therapy session: sessionId={}", query.sessionId());
        return therapySessionRepository.findById(TherapySessionId.of(query.sessionId()))
                .orElseThrow(() -> TherapySessionNotFoundException.withId(query.sessionId().toString()));
    }

    @Override
    public TherapySession handle(GetActiveTherapySessionByPatientIdQuery query) {
        log.debug("Finding active therapy session: patientId={}", query.patientId());
        ClinicId clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated clinic"));
        return therapySessionRepository.findActiveByPatientId(query.patientId(), clinicId.id(), ACTIVE_STATUSES)
                .orElseThrow(() -> TherapySessionNotFoundException.withId(
                        "active session for patient " + query.patientId()));
    }

    @Override
    public TherapySession handle(GetSessionProgressQuery query) {
        log.debug("Fetching session progress: sessionId={}", query.sessionId());
        return therapySessionRepository.findById(TherapySessionId.of(query.sessionId()))
                .orElseThrow(() -> TherapySessionNotFoundException.withId(query.sessionId().toString()));
    }

    @Override
    public TherapySession handle(GetSessionSummaryQuery query) {
        log.debug("Fetching session summary: sessionId={}", query.sessionId());
        TherapySession session = therapySessionRepository.findById(TherapySessionId.of(query.sessionId()))
                .orElseThrow(() -> TherapySessionNotFoundException.withId(query.sessionId().toString()));

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

        SerieId serieId = SerieId.of(query.serieId());
        return session.findSerie(serieId)
                .orElseThrow(() -> SerieNotFoundException.withId(query.serieId().toString()));
    }
}
