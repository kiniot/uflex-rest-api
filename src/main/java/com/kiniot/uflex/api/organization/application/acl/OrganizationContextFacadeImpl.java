package com.kiniot.uflex.api.organization.application.acl;

import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PatientRepository;
import com.kiniot.uflex.api.organization.interfaces.acl.OrganizationContextFacade;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.springframework.stereotype.Service;

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
}
