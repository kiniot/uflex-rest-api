package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

public record TierKitsResource(
        Integer baseKits,
        Boolean additionalKitsAllowed,
        Integer maxAdditionalKits
) {
}
