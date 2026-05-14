package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CheckoutSessionResult;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.StripeCheckoutSessionResource;

public class StripeCheckoutSessionResourceFromResultAssembler {
    public static StripeCheckoutSessionResource toResourceFromResult(CheckoutSessionResult result) {
        return new StripeCheckoutSessionResource(result.sessionId(), result.checkoutUrl());
    }
}
