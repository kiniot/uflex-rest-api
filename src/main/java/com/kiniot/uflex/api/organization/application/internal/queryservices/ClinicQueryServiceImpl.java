package com.kiniot.uflex.api.organization.application.internal.queryservices;

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

    public ClinicQueryServiceImpl(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
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