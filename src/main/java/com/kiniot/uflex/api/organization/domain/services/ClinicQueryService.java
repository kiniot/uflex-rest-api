package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Clinic;
import com.kiniot.uflex.api.organization.domain.model.queries.GetClinicByIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetClinicByRucQuery;

import java.util.Optional;

public interface ClinicQueryService {
    Optional<Clinic> handle(GetClinicByIdQuery query);
    Optional<Clinic> handle(GetClinicByRucQuery query);
}