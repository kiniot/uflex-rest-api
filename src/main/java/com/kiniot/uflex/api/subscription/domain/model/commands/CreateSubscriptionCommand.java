package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.exceptions.InvalidSubscriptionAmountFormatException;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionSelection;

public record CreateSubscriptionCommand(
        SubscriptionSelection selection,
        Money contractedPrice,
        Integer requestedTotalKits
) {
    public CreateSubscriptionCommand {
        if (selection == null)
            throw new IllegalArgumentException("Subscription selection cannot be null");
        if (contractedPrice == null)
            throw new IllegalArgumentException("Contracted price cannot be null");
        if (requestedTotalKits == null || requestedTotalKits < 0)
            throw new IllegalArgumentException("Requested total kits must be a non-negative integer");
        if (contractedPrice.amount().scale() != 2)
            throw new InvalidSubscriptionAmountFormatException(contractedPrice.amount());
    }
}
