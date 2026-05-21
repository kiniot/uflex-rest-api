package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionKitSelection;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionSelection;

import java.util.Objects;

public record CreateCheckoutSessionCommand(
        SubscriptionId subscriptionId,
        ClinicId clinicId,
        SubscriptionSelection selection,
        String tierName,
        Money amount,
        Money recurringPlanAmount,
        SubscriptionKitSelection kitSelection
) {
    public CreateCheckoutSessionCommand {
        Objects.requireNonNull(subscriptionId, "Subscription ID cannot be null");
        Objects.requireNonNull(clinicId, "Clinic ID cannot be null");
        Objects.requireNonNull(selection, "Subscription selection cannot be null");
        if (tierName == null || tierName.isBlank()) {
            throw new IllegalArgumentException("Tier name cannot be null or empty");
        }
        Objects.requireNonNull(amount, "Checkout amount cannot be null");
        Objects.requireNonNull(recurringPlanAmount, "Recurring plan amount cannot be null");
        Objects.requireNonNull(kitSelection, "Kit selection cannot be null");
    }
}
