package com.kiniot.uflex.api.subscription.domain.model.queries;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierId;

public record GetTierByIdQuery(TierId tierId) {
    public GetTierByIdQuery {
        if (tierId == null) {
            throw new IllegalArgumentException("Tier ID cannot be null");
        }
    }
}
