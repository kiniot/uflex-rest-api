package com.kiniot.uflex.api.organization.application.acl;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PatientRepository;
import com.kiniot.uflex.api.organization.interfaces.acl.OrganizationContextFacade;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrganizationContextFacadeImpl implements OrganizationContextFacade {

    private final PatientRepository patientRepository;

    public OrganizationContextFacadeImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public boolean existsPatientById(String patientId) {
        return patientRepository.existsById(new PatientId(UUID.fromString(patientId)));
    }

    @Override
    public boolean existsPatientByIdAndClinicId(String patientId, String clinicId) {
        return patientRepository.existsByIdAndClinicId(
                new PatientId(UUID.fromString(patientId)),
                new ClinicId(UUID.fromString(clinicId))
        );
    }

    @Override
    public List<String> findPatientIdsByPhysiotherapistIdAndClinicId(String physiotherapistId, String clinicId) {
        return patientRepository.findAllByAssignedPhysiotherapistIdAndClinicId(
                        new PhysiotherapistId(UUID.fromString(physiotherapistId)),
                        new ClinicId(UUID.fromString(clinicId))
                ).stream()
                .map(patient -> patient.getId().patientId().toString())
                .toList();
    }

    @Override
    public String findPatientIdByUserId(String userId) {
        return patientRepository.findByUserId(new UserId(UUID.fromString(userId)))
                .map(patient -> patient.getId().patientId().toString())
                .orElse("");
    }

    @Override
    public String getPatientFullName(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            return null;
        }
        return patientRepository.findById(new PatientId(UUID.fromString(patientId)))
                .map(this::formatPatientName)
                .orElse(null);
    }

    private String formatPatientName(Patient patient) {
        var firstName = patient.getFirstName() != null ? patient.getFirstName().firstName() : "";
        var lastName = patient.getLastName() != null ? patient.getLastName().lastName() : "";
        return (firstName + " " + lastName).trim();
    }
}
