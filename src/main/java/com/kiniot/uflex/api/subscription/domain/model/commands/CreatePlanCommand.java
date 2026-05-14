package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;

import java.util.List;

public record CreatePlanCommand(
        String name,
        String code,
        Money monthlyPrice,
        Money yearlyPrice,
        Integer maxPatients,
        Integer maxPhysiotherapists,
        List<String> features
) {
}
