package com.kiniot.uflex.api.organization.application.internal.commandservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Clinic;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterClinicCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdateClinicContactInfoCommand;
import com.kiniot.uflex.api.organization.domain.services.ClinicCommandService;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.ClinicRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClinicCommandServiceImpl implements ClinicCommandService {

    private final ClinicRepository clinicRepository;
    private final ExternalIamService externalIamService;

    public ClinicCommandServiceImpl(ClinicRepository clinicRepository, ExternalIamService externalIamService) {
        this.clinicRepository = clinicRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    @Transactional
    public Optional<Clinic> handle(RegisterClinicCommand command) {
        if (clinicRepository.existsByRuc(command.ruc())) {
            throw new ClinicAlreadyRegisteredException(command.ruc().ruc());
        }
        var clinic = new Clinic(command);
        clinic.register();
        return Optional.of(clinicRepository.save(clinic));
    }

    @Override
    @Transactional
    public void handle(UpdateClinicContactInfoCommand command) {
        var clinic = clinicRepository.findById(command.clinicId())
                .orElseThrow(() -> new ClinicNotFoundException("Clinic not found"));
        clinic.updateContactInfo(command.emailAddress(), command.phoneNumber());
        clinicRepository.save(clinic);
    }
}
