package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.entities.ClinicAdmin;
import com.kiniot.uflex.api.organization.domain.model.queries.GetClinicAdminByClinicIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetClinicAdminByUserIdQuery;

import java.util.Optional;

public interface ClinicAdminQueryService {
    Optional<ClinicAdmin> handle(GetClinicAdminByUserIdQuery query);
    Optional<ClinicAdmin> handle(GetClinicAdminByClinicIdQuery query);
}