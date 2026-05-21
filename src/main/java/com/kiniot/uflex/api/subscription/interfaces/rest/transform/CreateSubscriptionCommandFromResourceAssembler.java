package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.*;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CreateSubscriptionResource;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

public class CreateSubscriptionCommandFromResourceAssembler {
    public static CreateSubscriptionCommand toCommandFromResource(CreateSubscriptionResource resource) {
        return new CreateSubscriptionCommand(
                new SubscriptionSelection(
                        new TierId(UUID.fromString(resource.tierId())),
                        BillingPeriod.valueOf(resource.billingPeriod().trim().toUpperCase(Locale.ROOT))),
                new Money(
                        new BigDecimal(String.valueOf(resource.amount())),
                        CurrencyCode.valueOf(resource.currency().trim().toUpperCase(Locale.ROOT))
                ),
                resource.requestedTotalKits()
        );
    }
}
