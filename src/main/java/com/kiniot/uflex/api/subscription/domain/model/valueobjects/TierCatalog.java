package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import java.util.List;

public record TierCatalog(
        TierName name,
        TierLimits limits,
        TierKitPricing kitPricing,
        boolean allowsPriceOverride,
        List<TierCatalogPrice> prices
) {
    public TierCatalog {
        if (name == null)
            throw new IllegalArgumentException("Tier name is required");
        if (limits == null)
            throw new IllegalArgumentException("Tier limits is required");
        if (kitPricing == null)
            throw new IllegalArgumentException("Tier kitPricing is required");
        if (prices == null)
            throw new IllegalArgumentException("Tier prices is required");
    }
}
