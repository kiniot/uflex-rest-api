package com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment;

import com.kiniot.uflex.api.subscription.domain.model.commands.CreateCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.CheckoutSessionResult;

public interface PaymentGatewayPort {
    CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand command);
}
