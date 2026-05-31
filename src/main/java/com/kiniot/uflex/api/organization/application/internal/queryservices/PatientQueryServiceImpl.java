package com.kiniot.uflex.api.organization.application.internal.queryservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.queries.GetCurrentPatientQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientByIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientByUserIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientsByClinicIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientsByPhysiotherapistIdQuery;
import com.kiniot.uflex.api.organization.domain.services.PatientQueryService;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientQueryServiceImpl implements PatientQueryService {

    private final PatientRepository patientRepository;
    private final ExternalIamService externalIamService;

    public PatientQueryServiceImpl(
            PatientRepository patientRepository,
            ExternalIamService externalIamService
    ) {
        this.patientRepository = patientRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    public Optional<Patient> handle(GetCurrentPatientQuery query) {
        var userId = externalIamService.fetchCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        return patientRepository.findByUserId(userId);
    }

    @Override
    public Optional<Patient> handle(GetPatientByIdQuery query) {
        return patientRepository.findById(query.patientId());
    }

    @Override
    public Optional<Patient> handle(GetPatientByUserIdQuery query) {
        return patientRepository.findByUserId(query.userId());
    }

    @Override
    public List<Patient> handle(GetPatientsByClinicIdQuery query) {
        return patientRepository.findAllByClinicId(query.clinicId());
    }

    @Override
    public List<Patient> handle(GetPatientsByPhysiotherapistIdQuery query) {
        return patientRepository.findAllByAssignedPhysiotherapistId(query.physiotherapistId());
    }
}
