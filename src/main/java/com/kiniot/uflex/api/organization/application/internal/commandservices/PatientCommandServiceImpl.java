package com.kiniot.uflex.api.organization.application.internal.commandservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.CrossClinicAssignmentException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignPatientToPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DischargePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.organization.domain.services.PatientCommandService;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PatientRepository;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PhysiotherapistRepository;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PatientCommandServiceImpl implements PatientCommandService {

    private final PatientRepository patientRepository;
    private final PhysiotherapistRepository physiotherapistRepository;
    private final ExternalIamService externalIamService;

    public PatientCommandServiceImpl(
            PatientRepository patientRepository,
            PhysiotherapistRepository physiotherapistRepository,
            ExternalIamService externalIamService
    ) {
        this.patientRepository = patientRepository;
        this.physiotherapistRepository = physiotherapistRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    @Transactional
    public Optional<Patient> handle(RegisterPatientByClinicAdminCommand command) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ClinicNotFoundException("Current clinic not found"));
        validatePatientRegistration(command.emailAddress(), clinicId, command.assignedPhysiotherapistId());

        var userId = externalIamService.registerPatient(command.emailAddress().email())
                .orElseThrow(() -> new RuntimeException("Failed to register patient in IAM"));

        Patient patient;
        try {
            if (command.assignedPhysiotherapistId() != null) {
                var physiotherapist = physiotherapistRepository.findById(command.assignedPhysiotherapistId())
                        .orElseThrow(() -> new IllegalArgumentException("Physiotherapist not found"));
                patient = new Patient(command, userId, clinicId, physiotherapist.getClinicId());
            } else {
                patient = new Patient(command, userId, clinicId);
            }
            patient.register();
            return Optional.of(patientRepository.save(patient));
        } catch (RuntimeException exception) {
            compensateProvisionedPatientUser(userId, exception);
            throw exception;
        }
    }

    @Override
    @Transactional
    public Optional<Patient> handle(RegisterPatientByPhysiotherapistCommand command) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ClinicNotFoundException("Current clinic not found"));
        validatePatientRegistration(command.emailAddress(), clinicId, command.assignedPhysiotherapistId());

        var userId = externalIamService.registerPatient(command.emailAddress().email())
                .orElseThrow(() -> new RuntimeException("Failed to register patient in IAM"));

        try {
            var physiotherapist = physiotherapistRepository.findById(command.assignedPhysiotherapistId())
                    .orElseThrow(() -> new IllegalArgumentException("Physiotherapist not found"));
            var patient = new Patient(command, userId, clinicId, physiotherapist.getClinicId());
            patient.register();
            return Optional.of(patientRepository.save(patient));
        } catch (RuntimeException exception) {
            compensateProvisionedPatientUser(userId, exception);
            throw exception;
        }
    }

    private void validatePatientRegistration(
            com.kiniot.uflex.api.shared.domain.model.valueobjects.Email emailAddress,
            com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId clinicId,
            PhysiotherapistId assignedPhysiotherapistId
    ) {
        if (patientRepository.existsByEmailAddress(emailAddress)) {
            throw new PatientAlreadyRegisteredException(emailAddress.email());
        }

        if (assignedPhysiotherapistId == null) {
            return;
        }

        var physiotherapist = physiotherapistRepository.findById(assignedPhysiotherapistId)
                .orElseThrow(() -> new IllegalArgumentException("Physiotherapist not found"));
        if (!clinicId.equals(physiotherapist.getClinicId())) {
            throw new CrossClinicAssignmentException();
        }
    }

    private void compensateProvisionedPatientUser(UserId userId, RuntimeException originalException) {
        try {
            externalIamService.deleteUserById(userId);
        } catch (RuntimeException compensationException) {
            originalException.addSuppressed(compensationException);
        }
    }

    @Override
    @Transactional
    public void handle(AssignPatientToPhysiotherapistCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        var physiotherapist = physiotherapistRepository.findById(command.physiotherapistId())
                .orElseThrow(() -> new IllegalArgumentException("Physiotherapist not found"));
        if (!patient.getClinicId().equals(physiotherapist.getClinicId())) {
            throw new com.kiniot.uflex.api.organization.domain.exceptions.CrossClinicAssignmentException();
        }
        patient.assignPhysiotherapist(command.physiotherapistId(), physiotherapist.getClinicId());
        patientRepository.save(patient);
    }

    @Override
    @Transactional
    public void handle(DischargePatientCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        patient.discharge();
        patientRepository.save(patient);
    }

}
