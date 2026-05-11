package com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;

import java.util.Optional;

public interface PaymentGatewayPort {
    CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand command);
    PaymentReference charge(Money amount, String paymentToken);
    PaymentReference updatePaymentMethod(String paymentToken);
    boolean verifyWebhookSignature(String payload, String signature);
    Optional<CheckoutSessionCompletedPayment> handleCheckoutSessionCompleted(String payload, String signature);
}
