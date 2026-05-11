package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PlanResource(
        UUID id,
        String name,
        String code,
        BigDecimal monthlyPrice,
        BigDecimal yearlyPrice,
        String currency,
        Integer maxPatients,
        Integer maxPhysiotherapists,
        List<String> features,
        boolean active
) {
}
