package com.kiniot.uflex.api.organization.application.internal.commandservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.CrossClinicAssignmentException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.SuspendedPhysiotherapistAssignmentException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignPatientToPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.CompletePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DischargePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.MarkPatientInactiveCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.ReactivatePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistStatus;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;
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
    private final PhysiotherapistStatusSynchronizationService physiotherapistStatusSynchronizationService;

    public PatientCommandServiceImpl(
            PatientRepository patientRepository,
            PhysiotherapistRepository physiotherapistRepository,
            ExternalIamService externalIamService,
            PhysiotherapistStatusSynchronizationService physiotherapistStatusSynchronizationService
    ) {
        this.patientRepository = patientRepository;
        this.physiotherapistRepository = physiotherapistRepository;
        this.externalIamService = externalIamService;
        this.physiotherapistStatusSynchronizationService = physiotherapistStatusSynchronizationService;
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
                var physiotherapist = getAssignablePhysiotherapist(command.assignedPhysiotherapistId());
                patient = new Patient(command, userId, clinicId, physiotherapist.getClinicId());
            } else {
                patient = new Patient(command, userId, clinicId);
            }
            patient.register();
            var savedPatient = patientRepository.save(patient);
            synchronizeAssignedPhysiotherapist(savedPatient.getAssignedPhysiotherapistId());
            return Optional.of(savedPatient);
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
            var physiotherapist = getAssignablePhysiotherapist(command.assignedPhysiotherapistId());
            var patient = new Patient(command, userId, clinicId, physiotherapist.getClinicId());
            patient.register();
            var savedPatient = patientRepository.save(patient);
            synchronizeAssignedPhysiotherapist(savedPatient.getAssignedPhysiotherapistId());
            return Optional.of(savedPatient);
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
        if (physiotherapist.getStatus() == PhysiotherapistStatus.SUSPENDED) {
            throw new SuspendedPhysiotherapistAssignmentException();
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
        var previousPhysiotherapistId = patient.getAssignedPhysiotherapistId();
        var physiotherapist = getAssignablePhysiotherapist(command.physiotherapistId());
        if (!patient.getClinicId().equals(physiotherapist.getClinicId())) {
            throw new com.kiniot.uflex.api.organization.domain.exceptions.CrossClinicAssignmentException();
        }
        patient.assignPhysiotherapist(command.physiotherapistId(), physiotherapist.getClinicId());
        patientRepository.save(patient);
        synchronizeAssignedPhysiotherapist(previousPhysiotherapistId);
        synchronizeAssignedPhysiotherapist(command.physiotherapistId());
    }

    @Override
    @Transactional
    public void handle(CompletePatientCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        patient.complete();
        patientRepository.save(patient);
        synchronizeAssignedPhysiotherapist(patient.getAssignedPhysiotherapistId());
    }

    @Override
    @Transactional
    public void handle(MarkPatientInactiveCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        patient.markInactive();
        patientRepository.save(patient);
        synchronizeAssignedPhysiotherapist(patient.getAssignedPhysiotherapistId());
    }

    @Override
    @Transactional
    public void handle(ReactivatePatientCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        patient.reactivate();
        patientRepository.save(patient);
        synchronizeAssignedPhysiotherapist(patient.getAssignedPhysiotherapistId());
    }

    @Override
    @Transactional
    public void handle(DischargePatientCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        patient.discharge();
        patientRepository.save(patient);
        synchronizeAssignedPhysiotherapist(patient.getAssignedPhysiotherapistId());
    }

    private com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist getAssignablePhysiotherapist(PhysiotherapistId physiotherapistId) {
        var physiotherapist = physiotherapistRepository.findById(physiotherapistId)
                .orElseThrow(() -> new IllegalArgumentException("Physiotherapist not found"));
        if (physiotherapist.getStatus() == PhysiotherapistStatus.SUSPENDED) {
            throw new SuspendedPhysiotherapistAssignmentException();
        }
        return physiotherapist;
    }

    private void synchronizeAssignedPhysiotherapist(PhysiotherapistId physiotherapistId) {
        if (physiotherapistId == null) {
            return;
        }
        physiotherapistRepository.findById(physiotherapistId)
                .ifPresent(physiotherapistStatusSynchronizationService::synchronize);
    }

}
