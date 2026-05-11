package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.commands.ChangeSubscriptionPlanCommand;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.ChangePlanResource;

import java.util.UUID;

public class ChangeSubscriptionPlanCommandFromResourceAssembler {
    public static ChangeSubscriptionPlanCommand toCommandFromResource(UUID subscriptionId, ChangePlanResource resource) {
        return new ChangeSubscriptionPlanCommand(
                subscriptionId,
                resource.newPlanId(),
                resource.newBillingCycle(),
                resource.effectiveAt()
        );
    }
}
