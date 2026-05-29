package com.kiniot.uflex.api.subscription.domain.services;

import com.kiniot.uflex.api.subscription.domain.model.entities.Tier;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetAllTiersQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetTierByIdQuery;

import java.util.List;
import java.util.Optional;

public interface TierQueryService {
    List<Tier> handle(GetAllTiersQuery query);
    Optional<Tier> handle(GetTierByIdQuery query);
}
