package com.kiniot.uflex.api.organization.application.internal.queryservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.entities.ClinicAdmin;
import com.kiniot.uflex.api.organization.domain.model.queries.GetClinicAdminByClinicIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetClinicAdminByUserIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetCurrentClinicAdminQuery;
import com.kiniot.uflex.api.organization.domain.services.ClinicAdminQueryService;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.ClinicAdminRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClinicAdminQueryServiceImpl implements ClinicAdminQueryService {

    private final ClinicAdminRepository clinicAdminRepository;
    private final ExternalIamService externalIamService;

    public ClinicAdminQueryServiceImpl(
            ClinicAdminRepository clinicAdminRepository,
            ExternalIamService externalIamService
    ) {
        this.clinicAdminRepository = clinicAdminRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    public Optional<ClinicAdmin> handle(GetCurrentClinicAdminQuery query) {
        var userId = externalIamService.fetchCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        return clinicAdminRepository.findByUserId(userId);
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
