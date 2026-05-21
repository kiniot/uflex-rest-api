package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionSelection;

public record CreateSubscriptionCommand(
        SubscriptionSelection selection,
        Money contractedPrice
) {
    public CreateSubscriptionCommand {
        if (selection == null) {
            throw new IllegalArgumentException("Subscription selection cannot be null");
        }
        if (contractedPrice == null) {
            throw new IllegalArgumentException("Contracted price cannot be null");
        }
    }
}
