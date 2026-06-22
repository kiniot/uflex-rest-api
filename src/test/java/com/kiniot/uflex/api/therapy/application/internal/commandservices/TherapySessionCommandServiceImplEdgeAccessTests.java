package com.kiniot.uflex.api.therapy.application.internal.commandservices;

import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalDeviceService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalPlanningService;
import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.commands.RecordCompensatoryMovementCommand;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.KitSerial;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.TherapySessionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies per-edge least-privilege on the write path: a {@code ROLE_EDGE} caller may only
 * record against the session of the kit it is bound to.
 */
class TherapySessionCommandServiceImplEdgeAccessTests {

    private TherapySessionRepository repository;
    private ExternalIamService externalIamService;
    private TherapySessionCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(TherapySessionRepository.class);
        externalIamService = mock(ExternalIamService.class);
        service = new TherapySessionCommandServiceImpl(
                repository,
                mock(ApplicationEventPublisher.class),
                mock(ExternalPlanningService.class),
                externalIamService,
                mock(ExternalDeviceService.class),
                mock(ExternalOrganizationService.class));
        authenticateAsEdge();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void edgeCannotWriteForAnotherKitsSession() {
        var session = mock(TherapySession.class);
        when(session.getIotDeviceId()).thenReturn(KitSerial.of("kit-A"));
        when(repository.findById(any())).thenReturn(Optional.of(session));
        when(externalIamService.findEdgeSerialForCurrentUser()).thenReturn(Optional.of("kit-B"));

        assertThrows(AccessDeniedException.class, () -> service.handle(
                new RecordCompensatoryMovementCommand(UUID.randomUUID(), "ShoulderCompensation", UUID.randomUUID())));

        verify(repository, never()).save(any());
    }

    private void authenticateAsEdge() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "edge-principal", null, List.of(new SimpleGrantedAuthority("ROLE_EDGE")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
