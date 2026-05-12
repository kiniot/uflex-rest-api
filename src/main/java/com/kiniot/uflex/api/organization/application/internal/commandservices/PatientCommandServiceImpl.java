package com.kiniot.uflex.api.organization.application.internal.commandservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignPatientToPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignTreatmentPlanToPatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DischargePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.services.PatientCommandService;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PatientRepository;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PhysiotherapistRepository;
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
    public Optional<Patient> handle(RegisterPatientCommand command) {
        var userId = externalIamService.fetchCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ClinicNotFoundException("Current clinic not found"));

        if (patientRepository.existsByUserId(userId)) {
            throw new PatientAlreadyRegisteredException(userId.id().toString());
        }
        var patient = new Patient(command, new UserId(userId.id()), clinicId);
        patient.register();
        return Optional.of(patientRepository.save(patient));
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

    @Override
    @Transactional
    public void handle(AssignTreatmentPlanToPatientCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        patient.assignTreatmentPlan(command.treatmentPlanId());
        patientRepository.save(patient);
    }
}