package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionCheckoutResult;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CheckoutSessionResource;

public class CheckoutSessionResourceFromResultAssembler {
    public static CheckoutSessionResource toResourceFromResult(SubscriptionCheckoutResult result) {
        return new CheckoutSessionResource(
                result.subscriptionId().id().toString(),
                result.status().name(),
                result.checkoutUrl()
        );
    }
}
