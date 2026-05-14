package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateChangePlanCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.ChangePlanCheckoutSessionResource;

public class CreateChangePlanCheckoutSessionCommandFromResourceAssembler {
    public static CreateChangePlanCheckoutSessionCommand toCommandFromResource(ChangePlanCheckoutSessionResource resource,
                                                                               SubscriptionId subscriptionId,
                                                                               UserId userId) {
        return new CreateChangePlanCheckoutSessionCommand(
                subscriptionId,
                new SubscriptionPlanId(resource.planId()),
                BillingCycle.valueOf(resource.billingCycle()),
                userId
        );
    }
}
