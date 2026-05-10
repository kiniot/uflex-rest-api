package com.kiniot.uflex.api.organization.application.internal.commandservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.entities.ClinicAdmin;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.services.ClinicAdminCommandService;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.ClinicAdminRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClinicAdminCommandServiceImpl implements ClinicAdminCommandService {

    private final ClinicAdminRepository clinicAdminRepository;
    private final ExternalIamService externalIamService;
    private final ApplicationEventPublisher eventPublisher;

    public ClinicAdminCommandServiceImpl(
            ClinicAdminRepository clinicAdminRepository,
            ExternalIamService externalIamService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.clinicAdminRepository = clinicAdminRepository;
        this.externalIamService = externalIamService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Optional<ClinicAdmin> handle(RegisterClinicAdminCommand command) {
        var userId = externalIamService.fetchCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ClinicNotFoundException("Current clinic not found"));

        var clinicAdmin = new ClinicAdmin(command, userId, clinicId);
        var saved = clinicAdminRepository.save(clinicAdmin);
        eventPublisher.publishEvent(new com.kiniot.uflex.api.organization.domain.model.events.ClinicAdminRegisteredEvent(
                this,
                userId.id().toString(),
                clinicId.clinicId().toString()
        ));
        return Optional.of(saved);
    }
}