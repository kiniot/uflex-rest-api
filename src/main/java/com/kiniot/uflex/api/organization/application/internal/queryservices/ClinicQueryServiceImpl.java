package com.kiniot.uflex.api.organization.application.internal.queryservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.model.queries.GetCurrentClinicQuery;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Clinic;
import com.kiniot.uflex.api.organization.domain.model.queries.GetClinicByIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetClinicByRucQuery;
import com.kiniot.uflex.api.organization.domain.services.ClinicQueryService;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.ClinicRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClinicQueryServiceImpl implements ClinicQueryService {

    private final ClinicRepository clinicRepository;
    private final ExternalIamService externalIamService;

    public ClinicQueryServiceImpl(
            ClinicRepository clinicRepository,
            ExternalIamService externalIamService
    ) {
        this.clinicRepository = clinicRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    public Optional<Clinic> handle(GetCurrentClinicQuery query) {
        return externalIamService.fetchCurrentClinicId()
                .flatMap(clinicRepository::findById);
    }

    @Override
    public Optional<Clinic> handle(GetClinicByIdQuery query) {
        return clinicRepository.findById(query.clinicId());
    }

    @Override
    public Optional<Clinic> handle(GetClinicByRucQuery query) {
        return clinicRepository.findByRuc(query.ruc());
    }
}
