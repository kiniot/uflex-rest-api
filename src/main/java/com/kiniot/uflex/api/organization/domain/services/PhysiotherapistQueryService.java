package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistByIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistByUserIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistsByClinicIdQuery;

import java.util.List;
import java.util.Optional;

public interface PhysiotherapistQueryService {
    Optional<Physiotherapist> handle(GetPhysiotherapistByIdQuery query);
    Optional<Physiotherapist> handle(GetPhysiotherapistByUserIdQuery query);
    List<Physiotherapist> handle(GetPhysiotherapistsByClinicIdQuery query);
}