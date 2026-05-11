package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.commands.UpdatePaymentMethodCommand;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.UpdatePaymentMethodResource;

import java.util.UUID;

public class UpdatePaymentMethodCommandFromResourceAssembler {
    public static UpdatePaymentMethodCommand toCommandFromResource(UUID subscriptionId, UpdatePaymentMethodResource resource) {
        return new UpdatePaymentMethodCommand(
                subscriptionId,
                resource.paymentToken(),
                resource.last4(),
                resource.expiresOn()
        );
    }
}
