package com.kiniot.uflex.api.subscription.domain.model.commands;

import java.math.BigDecimal;
import java.util.List;

public record CreatePlanCommand(
        String name,
        String code,
        BigDecimal monthlyPrice,
        BigDecimal yearlyPrice,
        String currency,
        Integer maxPatients,
        Integer maxPhysiotherapists,
        List<String> features
) {
}
