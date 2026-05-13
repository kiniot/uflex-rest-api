package com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;

import java.util.List;
import java.util.Optional;

public interface PaymentGatewayPort {
    CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand command);
    Optional<CheckoutSessionCompletedPayment> confirmCheckoutSession(String sessionId);
    Optional<PaymentMethodDetails> getPaymentMethod(PaymentReference paymentReference);
    List<InvoiceDetails> getInvoices(PaymentReference paymentReference);
    PaymentReference charge(Money amount, String paymentToken);
    PaymentReference updatePaymentMethod(String paymentToken);
    boolean verifyWebhookSignature(String payload, String signature);
    Optional<CheckoutSessionCompletedPayment> handleCheckoutSessionCompleted(String payload, String signature);
}
