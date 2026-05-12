package com.kiniot.uflex.api.organization.application.internal.queryservices;

import com.kiniot.uflex.api.organization.domain.model.entities.ClinicAdmin;
import com.kiniot.uflex.api.organization.domain.model.queries.GetClinicAdminByClinicIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetClinicAdminByUserIdQuery;
import com.kiniot.uflex.api.organization.domain.services.ClinicAdminQueryService;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.ClinicAdminRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClinicAdminQueryServiceImpl implements ClinicAdminQueryService {

    private final ClinicAdminRepository clinicAdminRepository;

    public ClinicAdminQueryServiceImpl(ClinicAdminRepository clinicAdminRepository) {
        this.clinicAdminRepository = clinicAdminRepository;
    }

    @Override
    public Optional<ClinicAdmin> handle(GetClinicAdminByUserIdQuery query) {
        return clinicAdminRepository.findByUserId(query.userId());
    }

    @Override
    public Optional<ClinicAdmin> handle(GetClinicAdminByClinicIdQuery query) {
        return clinicAdminRepository.findByClinicId(query.clinicId());
    }
}