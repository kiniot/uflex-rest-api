package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.commands.PurchaseSubscriptionPlanCommand;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.PurchaseSubscriptionResource;

public class PurchaseSubscriptionCommandFromResourceAssembler {
    public static PurchaseSubscriptionPlanCommand toCommandFromResource(PurchaseSubscriptionResource resource) {
        return new PurchaseSubscriptionPlanCommand(
                resource.clinicId(),
                resource.planId(),
                resource.billingCycle(),
                resource.paymentToken()
        );
    }
}
