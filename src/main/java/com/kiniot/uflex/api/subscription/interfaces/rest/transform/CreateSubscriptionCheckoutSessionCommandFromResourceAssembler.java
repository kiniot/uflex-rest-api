package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CreateStripeCheckoutSessionResource;

public class CreateSubscriptionCheckoutSessionCommandFromResourceAssembler {
    public static CreateSubscriptionCheckoutSessionCommand toCommandFromResource(CreateStripeCheckoutSessionResource resource) {
        return new CreateSubscriptionCheckoutSessionCommand(
                resource.clinicId(),
                resource.planId(),
                BillingCycle.valueOf(resource.billingCycle())
        );
    }
}
