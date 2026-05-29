package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.entities.Tier;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.TierCatalogResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.TierKitPriceResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.TierKitsResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.TierLimitsResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.TierPriceResource;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TierCatalogResourceFromEntityAssembler {

    private TierCatalogResourceFromEntityAssembler() {
    }

    public static TierCatalogResource toResourceFromEntity(Tier entity) {
        var limits = toLimitsResource(entity);

        return new TierCatalogResource(
                Objects.requireNonNull(entity.getId()).id().toString(),
                entity.getName().name(),
                entity.isAllowsPriceOverride(),
                limits,
                new TierKitsResource(
                        entity.getTierKitPricing().baseKits(),
                        entity.getTierKitPricing().additionalKitsAllowed(),
                        entity.getTierKitPricing().maxAdditionalKits()
                ),
                entity.getPrices().stream()
                        .sorted(Comparator
                                .comparing((com.kiniot.uflex.api.subscription.domain.model.entities.TierPrice price) -> price.getBillingPeriod().ordinal())
                                .thenComparing(price -> price.getPrice().currency().ordinal()))
                        .map(price -> new TierPriceResource(
                                price.getBillingPeriod().name(),
                                price.getPrice().currency().name(),
                                price.getPrice().amount()
                        ))
                        .toList(),
                List.of(
                        new TierKitPriceResource(
                                entity.getTierKitPricing().penKitUnitPrice().currency().name(),
                                entity.getTierKitPricing().penKitUnitPrice().amount()
                        ),
                        new TierKitPriceResource(
                                entity.getTierKitPricing().usdKitUnitPrice().currency().name(),
                                entity.getTierKitPricing().usdKitUnitPrice().amount()
                        )
                )
        );
    }

    private static TierLimitsResource toLimitsResource(Tier entity) {
        if (entity.getTierLimits() == null) {
            return new TierLimitsResource(null, null, null);
        }

        return new TierLimitsResource(
                entity.getTierLimits().maxIotKits(),
                entity.getTierLimits().maxPatients(),
                entity.getTierLimits().maxPhysiotherapists()
        );
    }
}
