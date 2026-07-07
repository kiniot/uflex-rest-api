package com.kiniot.uflex.api.therapy.application.internal.queryservices;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalPlanningService;
import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.queries.GetActiveTherapySessionByDeviceSerialQuery;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.TherapySessionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies per-edge least-privilege on the device-serial read path: a {@code ROLE_EDGE}
 * caller may only query its own kit's serial.
 */
class TherapySessionQueryServiceImplEdgeAccessTests {

    private TherapySessionRepository repository;
    private ExternalIamService externalIamService;
    private TherapySessionQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(TherapySessionRepository.class);
        externalIamService = mock(ExternalIamService.class);
        service = new TherapySessionQueryServiceImpl(
                repository,
                externalIamService,
                mock(ExternalPlanningService.class),
                mock(ExternalOrganizationService.class));
        authenticateAsEdge();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void edgeCannotQueryAnotherKitsSerial() {
        when(externalIamService.findEdgeSerialForCurrentUser()).thenReturn(Optional.of("kit-A"));

        assertThrows(AccessDeniedException.class, () ->
                service.handle(new GetActiveTherapySessionByDeviceSerialQuery("kit-B")));

        verify(repository, never()).findActiveByIotDeviceId(any(), any(), any());
    }

    @Test
    void edgeWithoutKitBindingIsDenied() {
        when(externalIamService.findEdgeSerialForCurrentUser()).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () ->
                service.handle(new GetActiveTherapySessionByDeviceSerialQuery("kit-A")));
    }

    @Test
    void edgeCanQueryItsOwnKitsSerial() {
        var clinicId = new ClinicId(UUID.randomUUID());
        var session = mock(TherapySession.class);
        when(externalIamService.findEdgeSerialForCurrentUser()).thenReturn(Optional.of("kit-A"));
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.of(clinicId));
        when(repository.findActiveByIotDeviceId("kit-A", clinicId.id(), SessionStatus.ACTIVE_STATUSES))
                .thenReturn(Optional.of(session));

        var result = service.handle(new GetActiveTherapySessionByDeviceSerialQuery("kit-A"));

        assertEquals(session, result);
    }

    private void authenticateAsEdge() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "edge-principal", null, List.of(new SimpleGrantedAuthority("ROLE_EDGE")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
