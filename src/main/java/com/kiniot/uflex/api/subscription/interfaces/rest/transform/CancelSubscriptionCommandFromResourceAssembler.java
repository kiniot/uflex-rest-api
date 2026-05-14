package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.commands.CancelSubscriptionCommand;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CancelSubscriptionResource;

import java.util.UUID;

public class CancelSubscriptionCommandFromResourceAssembler {
    public static CancelSubscriptionCommand toCommandFromResource(UUID subscriptionId, CancelSubscriptionResource resource) {
        return new CancelSubscriptionCommand(subscriptionId, resource == null ? null : resource.reason());
    }
}
