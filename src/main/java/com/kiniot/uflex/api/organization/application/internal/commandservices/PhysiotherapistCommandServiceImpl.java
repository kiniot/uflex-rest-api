package com.kiniot.uflex.api.organization.application.internal.commandservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistLicenseInvalidException;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.services.PhysiotherapistCommandService;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PhysiotherapistRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PhysiotherapistCommandServiceImpl implements PhysiotherapistCommandService {

    private final PhysiotherapistRepository physiotherapistRepository;
    private final ExternalIamService externalIamService;

    public PhysiotherapistCommandServiceImpl(
            PhysiotherapistRepository physiotherapistRepository,
            ExternalIamService externalIamService
    ) {
        this.physiotherapistRepository = physiotherapistRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    @Transactional
    public Optional<Physiotherapist> handle(RegisterPhysiotherapistCommand command) {
        var userId = externalIamService.registerPhysiotherapist(command.emailAddress().email())
                .orElseThrow(() -> new RuntimeException("Failed to register physiotherapist in IAM"));
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ClinicNotFoundException("Current clinic not found"));

        if (physiotherapistRepository.existsByLicenseNumberAndClinicId(command.licenseNumber(), clinicId)) {
            throw new PhysiotherapistLicenseInvalidException(command.licenseNumber().licenseNumber());
        }
        if (physiotherapistRepository.existsByEmailAddressAndClinicId(command.emailAddress(), clinicId)) {
            throw new com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistAlreadyRegisteredException(
                    "Physiotherapist with email " + command.emailAddress().email() + " already registered for this clinic");
        }
        var physiotherapist = new Physiotherapist(command, new UserId(userId.id()), clinicId);
        physiotherapist.register();
        return Optional.of(physiotherapistRepository.save(physiotherapist));
    }
}