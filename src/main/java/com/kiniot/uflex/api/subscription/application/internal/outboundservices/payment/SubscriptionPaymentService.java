package com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;
import com.kiniot.uflex.api.subscription.domain.services.results.CheckoutSessionResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPaymentService {
    CheckoutSessionResult createCheckoutSession(
            UUID clinicId,
            UUID planId,
            BillingCycle billingCycle,
            Money amount,
            String successUrl,
            String cancelUrl,
            String planName,
            String userId
    );
    Optional<CompletedCheckoutPayment> confirmCheckoutSession(String sessionId);
    Optional<SubscriptionPaymentMethodDetails> getPaymentMethod(PaymentReference paymentReference);
    List<SubscriptionInvoiceDetails> getInvoices(PaymentReference paymentReference);
    PaymentReference charge(Money amount, String paymentToken);
    PaymentReference updatePaymentMethod(String paymentToken);
    boolean verifyWebhookSignature(String payload, String signature);
    Optional<CompletedCheckoutPayment> handleCheckoutSessionCompleted(String payload, String signature);
}
