package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CheckoutSessionCompletedPayment;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CheckoutSessionResult;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CreateCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentGatewayPort;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;
import com.kiniot.uflex.api.subscription.infrastructure.configuration.StripePaymentProperties;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true")
public class StripePaymentAdapter implements PaymentGatewayPort {
    private final StripePaymentProperties properties;

    public StripePaymentAdapter(StripePaymentProperties properties) {
        this.properties = properties;
    }

    @Override
    public CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand command) {
        requireConfigured(properties.getSecretKey(), "Stripe secret key is not configured");
        Stripe.apiKey = properties.getSecretKey();
        try {
            var params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(urlOrDefault(command.successUrl(), properties.getSuccessUrl()))
                    .setCancelUrl(urlOrDefault(command.cancelUrl(), properties.getCancelUrl()))
                    .putMetadata("clinicId", command.clinicId().toString())
                    .putMetadata("planId", command.planId().toString())
                    .putMetadata("billingCycle", command.billingCycle().name())
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(command.currency().toLowerCase())
                                    .setUnitAmount(toMinorUnits(command.amount()))
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(command.planName())
                                            .build())
                                    .build())
                            .build())
                    .build();
            var session = Session.create(params);
            return new CheckoutSessionResult(session.getId(), session.getUrl());
        } catch (Exception exception) {
            throw new IllegalStateException("Stripe Checkout Session could not be created", exception);
        }
    }

    @Override
    public PaymentReference charge(Money amount, String paymentToken) {
        throw new IllegalStateException("Direct card charges are not supported. Use Stripe Checkout Session.");
    }

    @Override
    public PaymentReference updatePaymentMethod(String paymentToken) {
        throw new IllegalStateException("Direct payment method updates are not supported. Use Stripe Checkout.");
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        requireConfigured(properties.getWebhookSecret(), "Stripe webhook secret is not configured");
        try {
            Webhook.constructEvent(payload, signature, properties.getWebhookSecret());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public Optional<CheckoutSessionCompletedPayment> handleCheckoutSessionCompleted(String payload, String signature) {
        requireConfigured(properties.getWebhookSecret(), "Stripe webhook secret is not configured");
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, properties.getWebhookSecret());
        } catch (Exception exception) {
            throw new IllegalArgumentException("Stripe webhook payload could not be parsed", exception);
        }
        if (!"checkout.session.completed".equals(event.getType())) {
            // TODO: handle payment_intent.payment_failed, customer.subscription.deleted,
            // invoice.payment_succeeded and invoice.payment_failed.
            return Optional.empty();
        }
        return event.getDataObjectDeserializer().getObject()
                .filter(Session.class::isInstance)
                .map(Session.class::cast)
                .map(this::toCompletedPayment);
    }

    private CheckoutSessionCompletedPayment toCompletedPayment(Session session) {
        var metadata = session.getMetadata();
        var providerTransactionId = session.getPaymentIntent() == null ? session.getId() : session.getPaymentIntent();
        return new CheckoutSessionCompletedPayment(
                UUID.fromString(metadata.get("clinicId")),
                UUID.fromString(metadata.get("planId")),
                BillingCycle.valueOf(metadata.get("billingCycle")),
                new PaymentReference("STRIPE", providerTransactionId, null, null)
        );
    }

    private long toMinorUnits(BigDecimal amount) {
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private String urlOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private void requireConfigured(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
    }
}
