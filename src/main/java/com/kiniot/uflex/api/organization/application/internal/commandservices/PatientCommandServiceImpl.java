package com.kiniot.uflex.api.organization.application.internal.commandservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalPlanningService;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.CrossClinicAssignmentException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientAccessDeniedException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientClinicMismatchException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientHasTreatmentPlansException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.SuspendedPhysiotherapistAssignmentException;
import com.kiniot.uflex.api.organization.domain.exceptions.UserProvisioningException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignPatientToPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.CompletePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DeletePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DischargePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.MarkPatientInactiveCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.ReactivatePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdateCurrentPatientProfileCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdatePatientByClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdatePatientByPhysiotherapistCommand;
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
    private final ExternalPlanningService externalPlanningService;
    private final PhysiotherapistStatusSynchronizationService physiotherapistStatusSynchronizationService;

    public PatientCommandServiceImpl(
            PatientRepository patientRepository,
            PhysiotherapistRepository physiotherapistRepository,
            ExternalIamService externalIamService,
            ExternalPlanningService externalPlanningService,
            PhysiotherapistStatusSynchronizationService physiotherapistStatusSynchronizationService
    ) {
        this.patientRepository = patientRepository;
        this.physiotherapistRepository = physiotherapistRepository;
        this.externalIamService = externalIamService;
        this.externalPlanningService = externalPlanningService;
        this.physiotherapistStatusSynchronizationService = physiotherapistStatusSynchronizationService;
    }

    @Override
    @Transactional
    public Optional<Patient> handle(RegisterPatientByClinicAdminCommand command) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ClinicNotFoundException("Current clinic not found"));
        validatePatientRegistration(command.emailAddress(), clinicId, command.assignedPhysiotherapistId());

        var userId = externalIamService.registerPatient(command.emailAddress().email())
                .orElseThrow(() -> new UserProvisioningException("Failed to register patient in IAM"));

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
                .orElseThrow(() -> new UserProvisioningException("Failed to register patient in IAM"));

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

    @Override
    @Transactional
    public Optional<Patient> handle(UpdatePatientByClinicAdminCommand command) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ClinicNotFoundException("Current clinic not found"));
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        if (!patient.getClinicId().equals(clinicId)) {
            throw new PatientClinicMismatchException();
        }
        validateEmailUpdate(patient, command.emailAddress());
        var previousPhysiotherapistId = patient.getAssignedPhysiotherapistId();
        patient.updateByClinicAdmin(
                command.firstName(),
                command.lastName(),
                command.dni(),
                command.birthDate(),
                command.gender(),
                command.emailAddress(),
                command.phoneNumber(),
                command.medicalCondition()
        );
        updatePatientAssignment(patient, command.assignedPhysiotherapistId());
        var savedPatient = patientRepository.save(patient);
        synchronizeEmailIfChanged(savedPatient.getUserId(), patient.getEmailAddress(), command.emailAddress());
        synchronizeAssignedPhysiotherapist(previousPhysiotherapistId);
        synchronizeAssignedPhysiotherapist(savedPatient.getAssignedPhysiotherapistId());
        return Optional.of(savedPatient);
    }

    @Override
    @Transactional
    public Optional<Patient> handle(UpdatePatientByPhysiotherapistCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        validateCurrentPhysiotherapistOwnsPatient(patient);
        validateEmailUpdate(patient, command.emailAddress());
        var previousEmail = patient.getEmailAddress();
        patient.updateByPhysiotherapist(
                command.firstName(),
                command.lastName(),
                command.emailAddress(),
                command.phoneNumber(),
                command.medicalCondition()
        );
        var savedPatient = patientRepository.save(patient);
        synchronizeEmailIfChanged(savedPatient.getUserId(), previousEmail, command.emailAddress());
        return Optional.of(savedPatient);
    }

    @Override
    @Transactional
    public Optional<Patient> handle(UpdateCurrentPatientProfileCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        validateCurrentPatientOwnsProfile(patient);
        validateEmailUpdate(patient, command.emailAddress());
        var previousEmail = patient.getEmailAddress();
        patient.updateContactInformation(command.emailAddress(), command.phoneNumber());
        var savedPatient = patientRepository.save(patient);
        synchronizeEmailIfChanged(savedPatient.getUserId(), previousEmail, command.emailAddress());
        return Optional.of(savedPatient);
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
                .orElseThrow(() -> new PhysiotherapistNotFoundException("Physiotherapist not found"));
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
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
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
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        patient.complete();
        patientRepository.save(patient);
        synchronizeAssignedPhysiotherapist(patient.getAssignedPhysiotherapistId());
    }

    @Override
    @Transactional
    public void handle(MarkPatientInactiveCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        patient.markInactive();
        patientRepository.save(patient);
        synchronizeAssignedPhysiotherapist(patient.getAssignedPhysiotherapistId());
    }

    @Override
    @Transactional
    public void handle(ReactivatePatientCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        patient.reactivate();
        patientRepository.save(patient);
        synchronizeAssignedPhysiotherapist(patient.getAssignedPhysiotherapistId());
    }

    @Override
    @Transactional
    public void handle(DischargePatientCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        patient.discharge();
        patientRepository.save(patient);
        synchronizeAssignedPhysiotherapist(patient.getAssignedPhysiotherapistId());
    }

    @Override
    @Transactional
    public void handle(DeletePatientCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        if (externalPlanningService.existsTreatmentPlanByPatientId(patient.getId())) {
            throw new PatientHasTreatmentPlansException(patient.getId().patientId().toString());
        }
        var assignedPhysiotherapistId = patient.getAssignedPhysiotherapistId();
        var userId = patient.getUserId();
        patientRepository.delete(patient);
        synchronizeAssignedPhysiotherapist(assignedPhysiotherapistId);
        externalIamService.deleteUserById(userId);
    }

    private com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist getAssignablePhysiotherapist(PhysiotherapistId physiotherapistId) {
        var physiotherapist = physiotherapistRepository.findById(physiotherapistId)
                .orElseThrow(() -> new PhysiotherapistNotFoundException("Physiotherapist not found"));
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

    private void validateEmailUpdate(Patient patient, com.kiniot.uflex.api.shared.domain.model.valueobjects.Email updatedEmail) {
        if (!patient.getEmailAddress().equals(updatedEmail) && patientRepository.existsByEmailAddress(updatedEmail)) {
            throw new PatientAlreadyRegisteredException(updatedEmail.email());
        }
    }

    private void synchronizeEmailIfChanged(UserId userId,
                                           com.kiniot.uflex.api.shared.domain.model.valueobjects.Email previousEmail,
                                           com.kiniot.uflex.api.shared.domain.model.valueobjects.Email newEmail) {
        if (!previousEmail.equals(newEmail)) {
            externalIamService.updateUserEmail(userId, newEmail.email());
        }
    }

    private void updatePatientAssignment(Patient patient, PhysiotherapistId assignedPhysiotherapistId) {
        if (assignedPhysiotherapistId == null) {
            patient.unassignPhysiotherapist();
            return;
        }
        var physiotherapist = getAssignablePhysiotherapist(assignedPhysiotherapistId);
        if (!patient.getClinicId().equals(physiotherapist.getClinicId())) {
            throw new CrossClinicAssignmentException();
        }
        patient.assignPhysiotherapist(assignedPhysiotherapistId, physiotherapist.getClinicId());
    }

    private void validateCurrentPhysiotherapistOwnsPatient(Patient patient) {
        var currentPhysiotherapist = physiotherapistRepository.findByUserId(
                        externalIamService.fetchCurrentUserId()
                                .orElseThrow(() -> new ClinicNotFoundException("Current user not found")))
                .orElseThrow(() -> new PhysiotherapistNotFoundException("Physiotherapist not found"));
        if (patient.getAssignedPhysiotherapistId() == null || !patient.getAssignedPhysiotherapistId().equals(currentPhysiotherapist.getId())) {
            throw new PatientAccessDeniedException("You can only manage your own patients");
        }
    }

    private void validateCurrentPatientOwnsProfile(Patient patient) {
        var currentUserId = externalIamService.fetchCurrentUserId()
                .orElseThrow(() -> new ClinicNotFoundException("Current user not found"));
        if (!patient.getUserId().equals(currentUserId)) {
            throw new PatientAccessDeniedException("You can only edit your own patient profile");
        }
    }

}
