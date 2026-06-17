package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record TierLimits(
        Integer maxIotKits,
        Integer maxPatients,
        Integer maxPhysiotherapists
) {
}
