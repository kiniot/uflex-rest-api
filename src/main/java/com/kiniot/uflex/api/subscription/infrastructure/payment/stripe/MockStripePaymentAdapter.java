package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CheckoutSessionCompletedPayment;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CheckoutSessionResult;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CreateCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.InvoiceDetails;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentGatewayPort;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentMethodDetails;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockStripePaymentAdapter implements PaymentGatewayPort {
    private final Map<String, CreateCheckoutSessionCommand> sessions = new ConcurrentHashMap<>();

    @Override
    public CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand command) {
        var sessionId = "cs_test_mock_subscription";
        sessions.put(sessionId, command);
        return new CheckoutSessionResult(sessionId, "http://localhost:4200/subscription?payment=success&session_id=" + sessionId);
    }

    @Override
    public Optional<CheckoutSessionCompletedPayment> confirmCheckoutSession(String sessionId) {
        var command = sessions.get(sessionId);
        if (command == null) {
            return Optional.empty();
        }
        return Optional.of(new CheckoutSessionCompletedPayment(
                command.clinicId(),
                command.planId(),
                command.billingCycle(),
                new PaymentReference("STRIPE_MOCK", "pi_mock_subscription", sessionId, "cus_mock_subscription", "sub_mock_subscription", "4242", "12/2030"),
                OffsetDateTime.now(),
                command.billingCycle().name().equals("YEARLY") ? OffsetDateTime.now().plusYears(1) : OffsetDateTime.now().plusMonths(1)
        ));
    }

    @Override
    public Optional<PaymentMethodDetails> getPaymentMethod(PaymentReference paymentReference) {
        if (paymentReference == null || paymentReference.providerSubscriptionId() == null) return Optional.empty();
        return Optional.of(new PaymentMethodDetails("visa", "4242", 12, 2030));
    }

    @Override
    public List<InvoiceDetails> getInvoices(PaymentReference paymentReference) {
        if (paymentReference == null || paymentReference.providerSubscriptionId() == null) return List.of();
        return List.of(new InvoiceDetails(
                "in_mock_subscription",
                "MOCK-001",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                BigDecimal.valueOf(149),
                "PEN",
                "paid",
                "http://localhost:4200/mock-invoice"
        ));
    }

    @Override
    public PaymentReference charge(Money amount, String paymentToken) {
        if (paymentToken == null || paymentToken.isBlank() || "fail".equalsIgnoreCase(paymentToken)) {
            throw new IllegalStateException("Mock payment rejected");
        }
        return new PaymentReference("STRIPE_MOCK", "pi_mock_subscription", null, null, null, null, null);
    }

    @Override
    public PaymentReference updatePaymentMethod(String paymentToken) {
        if (paymentToken == null || paymentToken.isBlank() || "fail".equalsIgnoreCase(paymentToken)) {
            throw new IllegalStateException("Mock payment method rejected");
        }
        return new PaymentReference("STRIPE_MOCK", "pm_mock_subscription", null, null, null, "4242", null);
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        return true;
    }

    @Override
    public Optional<CheckoutSessionCompletedPayment> handleCheckoutSessionCompleted(String payload, String signature) {
        // TODO: add a dedicated mock webhook command if local tests need to simulate activation.
        return Optional.empty();
    }
}
