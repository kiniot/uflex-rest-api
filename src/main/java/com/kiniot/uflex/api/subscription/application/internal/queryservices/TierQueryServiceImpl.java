package com.kiniot.uflex.api.subscription.application.internal.queryservices;

import com.kiniot.uflex.api.subscription.domain.model.entities.Tier;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetAllTiersQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetTierByIdQuery;
import com.kiniot.uflex.api.subscription.domain.services.TierQueryService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.TierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TierQueryServiceImpl implements TierQueryService {

    private final TierRepository tierRepository;

    public TierQueryServiceImpl(TierRepository tierRepository) {
        this.tierRepository = tierRepository;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Tier> handle(GetAllTiersQuery query) {
        return tierRepository.findAllWithPricesBy().stream()
                .sorted(Comparator.comparing(tier -> tier.getName().ordinal()))
                .toList();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<Tier> handle(GetTierByIdQuery query) {
        return tierRepository.findWithPricesById(query.tierId());
    }
}
