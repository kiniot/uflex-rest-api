package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CreateStripeCheckoutSessionResource;

public class CreateSubscriptionCheckoutSessionCommandFromResourceAssembler {
    public static CreateSubscriptionCheckoutSessionCommand toCommandFromResource(
            CreateStripeCheckoutSessionResource resource,
            ClinicId clinicId,
            UserId userId
    ) {
        return new CreateSubscriptionCheckoutSessionCommand(
                clinicId,
                new SubscriptionPlanId(resource.planId()),
                BillingCycle.valueOf(resource.billingCycle()),
                userId
        );
    }
}
