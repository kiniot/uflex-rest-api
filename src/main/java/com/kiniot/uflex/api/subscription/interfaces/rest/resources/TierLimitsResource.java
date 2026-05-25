package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

public record TierLimitsResource(
        Integer maxIotKits,
        Integer maxPatients,
        Integer maxPhysiotherapists
) {
}
