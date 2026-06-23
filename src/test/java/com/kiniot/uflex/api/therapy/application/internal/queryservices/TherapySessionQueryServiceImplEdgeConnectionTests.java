package com.kiniot.uflex.api.therapy.application.internal.queryservices;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalPlanningService;
import com.kiniot.uflex.api.therapy.domain.exceptions.TherapySessionNotFoundException;
import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.queries.EdgeConnection;
import com.kiniot.uflex.api.therapy.domain.model.queries.GetEdgeConnectionForCurrentPatientQuery;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.KitSerial;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.TherapySessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the rendezvous read path: a patient resolves the LAN URL + pairing token of its
 * edge from its own active session.
 */
class TherapySessionQueryServiceImplEdgeConnectionTests {

    private TherapySessionRepository repository;
    private ExternalIamService externalIamService;
    private ExternalOrganizationService externalOrganizationService;
    private TherapySessionQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(TherapySessionRepository.class);
        externalIamService = mock(ExternalIamService.class);
        externalOrganizationService = mock(ExternalOrganizationService.class);
        service = new TherapySessionQueryServiceImpl(
                repository, externalIamService, mock(ExternalPlanningService.class), externalOrganizationService);
    }

    @Test
    void returnsLanUrlAndTokenForActiveSession() {
        var patientId = PatientId.of(UUID.randomUUID());
        var clinicId = new ClinicId(UUID.randomUUID());
        var session = mock(TherapySession.class);
        when(externalOrganizationService.fetchCurrentPatientId()).thenReturn(Optional.of(patientId));
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.of(clinicId));
        when(repository.findActiveByPatientId(patientId.id(), clinicId.id(), SessionStatus.ACTIVE_STATUSES))
                .thenReturn(Optional.of(session));
        when(session.getIotDeviceId()).thenReturn(KitSerial.of("kit-A"));
        when(session.getEdgePairingToken()).thenReturn("tok-123");
        when(externalIamService.findEdgeLanUrlBySerial("kit-A")).thenReturn(Optional.of("http://192.168.1.4:5050"));

        EdgeConnection result = service.handle(new GetEdgeConnectionForCurrentPatientQuery());

        assertEquals("http://192.168.1.4:5050", result.localEdgeUrl());
        assertEquals("tok-123", result.pairingToken());
        assertNull(result.expiresAt());
    }

    @Test
    void returnsNullUrlWhenEdgeHasNotReportedYet() {
        var patientId = PatientId.of(UUID.randomUUID());
        var clinicId = new ClinicId(UUID.randomUUID());
        var session = mock(TherapySession.class);
        when(externalOrganizationService.fetchCurrentPatientId()).thenReturn(Optional.of(patientId));
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.of(clinicId));
        when(repository.findActiveByPatientId(patientId.id(), clinicId.id(), SessionStatus.ACTIVE_STATUSES))
                .thenReturn(Optional.of(session));
        when(session.getIotDeviceId()).thenReturn(KitSerial.of("kit-A"));
        when(session.getEdgePairingToken()).thenReturn("tok-123");
        when(externalIamService.findEdgeLanUrlBySerial("kit-A")).thenReturn(Optional.empty());

        EdgeConnection result = service.handle(new GetEdgeConnectionForCurrentPatientQuery());

        assertNull(result.localEdgeUrl());
        assertEquals("tok-123", result.pairingToken());
    }

    @Test
    void throwsWhenNoActiveSession() {
        var patientId = PatientId.of(UUID.randomUUID());
        var clinicId = new ClinicId(UUID.randomUUID());
        when(externalOrganizationService.fetchCurrentPatientId()).thenReturn(Optional.of(patientId));
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.of(clinicId));
        when(repository.findActiveByPatientId(patientId.id(), clinicId.id(), SessionStatus.ACTIVE_STATUSES))
                .thenReturn(Optional.empty());

        assertThrows(TherapySessionNotFoundException.class, () ->
                service.handle(new GetEdgeConnectionForCurrentPatientQuery()));
    }

    @Test
    void deniesWhenNoCurrentPatient() {
        when(externalOrganizationService.fetchCurrentPatientId()).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () ->
                service.handle(new GetEdgeConnectionForCurrentPatientQuery()));
    }
}
