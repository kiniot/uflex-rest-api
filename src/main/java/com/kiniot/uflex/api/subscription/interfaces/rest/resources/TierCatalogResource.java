package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import java.util.List;

public record TierCatalogResource(
        String id,
        String name,
        boolean allowsPriceOverride,
        TierLimitsResource limits,
        TierKitsResource kits,
        List<TierPriceResource> prices,
        List<TierKitPriceResource> kitPrices
) {
}
