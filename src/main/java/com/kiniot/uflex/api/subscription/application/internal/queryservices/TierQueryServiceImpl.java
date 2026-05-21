package com.kiniot.uflex.api.subscription.application.internal.queryservices;

import com.kiniot.uflex.api.subscription.domain.model.entities.Tier;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetTierByIdQuery;
import com.kiniot.uflex.api.subscription.domain.services.TierQueryService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.TierRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TierQueryServiceImpl implements TierQueryService {

    private final TierRepository tierRepository;

    public TierQueryServiceImpl(TierRepository tierRepository) {
        this.tierRepository = tierRepository;
    }

    @Override
    public Optional<Tier> handle(GetTierByIdQuery query) {
        return tierRepository.findById(query.tierId());
    }
}
