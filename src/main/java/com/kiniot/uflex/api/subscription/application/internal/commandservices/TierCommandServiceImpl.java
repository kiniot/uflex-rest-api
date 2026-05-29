package com.kiniot.uflex.api.subscription.application.internal.commandservices;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierCatalogSeedData;
import com.kiniot.uflex.api.subscription.domain.model.commands.SeedTiersCommand;
import com.kiniot.uflex.api.subscription.domain.model.entities.Tier;
import com.kiniot.uflex.api.subscription.domain.services.TierCommandService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.TierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TierCommandServiceImpl  implements TierCommandService {

    private final TierRepository tierRepository;

    public TierCommandServiceImpl(TierRepository tierRepository) {
        this.tierRepository = tierRepository;
    }

    @Override
    @Transactional
    public void handle(SeedTiersCommand command) {
        TierCatalogSeedData.catalogs().forEach(catalog -> {
            var tier = tierRepository.findWithPricesByName(catalog.name())
                    .orElseGet(() -> Tier.create(
                            catalog.name(),
                            catalog.limits(),
                            catalog.kitPricing(),
                            catalog.allowsPriceOverride(),
                            java.util.List.of()
                    ));
            tier.updateLimits(catalog.limits());
            tier.updateKitPricing(catalog.kitPricing());
            tier.updatePriceOverridePolicy(catalog.allowsPriceOverride());
            catalog.prices().forEach(price -> {
                if (!tier.hasPrice(price.billingPeriod(), price.price().currency())) {
                    tier.addPrice(price.billingPeriod(), price.price());
                }
            });
            tierRepository.save(tier);
        });
    }
}
