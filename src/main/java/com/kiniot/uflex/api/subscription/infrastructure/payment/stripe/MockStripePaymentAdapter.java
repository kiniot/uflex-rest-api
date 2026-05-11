package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CheckoutSessionCompletedPayment;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CheckoutSessionResult;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CreateCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentGatewayPort;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockStripePaymentAdapter implements PaymentGatewayPort {
    @Override
    public CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand command) {
        return new CheckoutSessionResult("cs_test_mock_subscription", "http://localhost:4200/subscription?payment=success");
    }

    @Override
    public PaymentReference charge(Money amount, String paymentToken) {
        if (paymentToken == null || paymentToken.isBlank() || "fail".equalsIgnoreCase(paymentToken)) {
            throw new IllegalStateException("Mock payment rejected");
        }
        return new PaymentReference("STRIPE_MOCK", "pi_mock_subscription", null, null);
    }

    @Override
    public PaymentReference updatePaymentMethod(String paymentToken) {
        if (paymentToken == null || paymentToken.isBlank() || "fail".equalsIgnoreCase(paymentToken)) {
            throw new IllegalStateException("Mock payment method rejected");
        }
        return new PaymentReference("STRIPE_MOCK", "pm_mock_subscription", "4242", null);
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
