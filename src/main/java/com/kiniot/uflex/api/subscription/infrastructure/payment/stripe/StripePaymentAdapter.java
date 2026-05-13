package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CheckoutSessionCompletedPayment;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CheckoutSessionResult;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CreateCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.InvoiceDetails;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentGatewayPort;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentMethodDetails;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;
import com.kiniot.uflex.api.subscription.infrastructure.configuration.StripePaymentProperties;
import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.exception.StripeException;
import com.stripe.net.Webhook;
import com.stripe.param.InvoiceListParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true")
public class StripePaymentAdapter implements PaymentGatewayPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(StripePaymentAdapter.class);

    private final StripePaymentProperties properties;

    public StripePaymentAdapter(StripePaymentProperties properties) {
        this.properties = properties;
    }

    @Override
    public CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand command) {
        Stripe.apiKey = requireStripeTestSecretKey();
        var successUrl = requireConfigured(urlOrDefault(command.successUrl(), properties.getSuccessUrl()), "Stripe success URL is not configured");
        var cancelUrl = requireConfigured(urlOrDefault(command.cancelUrl(), properties.getCancelUrl()), "Stripe cancel URL is not configured");
        var currency = requireConfigured(command.currency(), "Stripe currency is not configured").toLowerCase();
        var unitAmount = toMinorUnits(command.amount());
        var planName = requireConfigured(command.planName(), "Stripe product name is not configured");
        try {
            var params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(withCheckoutSessionId(successUrl))
                    .setCancelUrl(cancelUrl)
                    .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                            .putMetadata("tenantId", command.clinicId().toString())
                            .putMetadata("clinicId", command.clinicId().toString())
                            .putMetadata("planId", command.planId().toString())
                            .putMetadata("billingCycle", command.billingCycle().name())
                            .putMetadata("userId", nullToEmpty(command.userId()))
                            .build())
                    .putMetadata("tenantId", command.clinicId().toString())
                    .putMetadata("clinicId", command.clinicId().toString())
                    .putMetadata("planId", command.planId().toString())
                    .putMetadata("billingCycle", command.billingCycle().name())
                    .putMetadata("userId", nullToEmpty(command.userId()))
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(currency)
                                    .setUnitAmount(unitAmount)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(planName)
                                            .build())
                                    .build())
                            .build())
                    .build();
            var session = Session.create(params);
            return new CheckoutSessionResult(session.getId(), session.getUrl());
        } catch (StripeException exception) {
            logStripeException("Stripe Checkout Session creation failed", exception);
            throw new IllegalStateException("Stripe Checkout Session could not be created: " + safeStripeMessage(exception), exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Stripe Checkout Session could not be created", exception);
        }
    }

    @Override
    public Optional<CheckoutSessionCompletedPayment> confirmCheckoutSession(String sessionId) {
        requireConfigured(properties.getSecretKey(), "Stripe secret key is not configured");
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Stripe Checkout Session id is required");
        }
        Stripe.apiKey = properties.getSecretKey();
        try {
            var params = SessionRetrieveParams.builder()
                    .addExpand("payment_intent.payment_method")
                    .addExpand("subscription")
                    .build();
            var session = Session.retrieve(sessionId, params, null);
            return Optional.of(toCompletedPayment(session));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Stripe Checkout Session could not be confirmed", exception);
        }
    }

    @Override
    public Optional<PaymentMethodDetails> getPaymentMethod(PaymentReference paymentReference) {
        requireConfigured(properties.getSecretKey(), "Stripe secret key is not configured");
        Stripe.apiKey = properties.getSecretKey();
        try {
            var paymentMethodId = findPaymentMethodId(paymentReference);
            if (isBlank(paymentMethodId)) return Optional.empty();
            var paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            if (paymentMethod.getCard() == null) return Optional.empty();
            var card = paymentMethod.getCard();
            return Optional.of(new PaymentMethodDetails(
                    card.getBrand(),
                    card.getLast4(),
                    card.getExpMonth() == null ? null : card.getExpMonth().intValue(),
                    card.getExpYear() == null ? null : card.getExpYear().intValue()
            ));
        } catch (Exception exception) {
            throw new IllegalStateException("Stripe payment method could not be retrieved", exception);
        }
    }

    @Override
    public List<InvoiceDetails> getInvoices(PaymentReference paymentReference) {
        requireConfigured(properties.getSecretKey(), "Stripe secret key is not configured");
        Stripe.apiKey = properties.getSecretKey();
        try {
            var builder = InvoiceListParams.builder().setLimit(20L);
            if (!isBlank(paymentReference.providerSubscriptionId())) {
                builder.setSubscription(paymentReference.providerSubscriptionId());
            } else if (!isBlank(paymentReference.providerCustomerId())) {
                builder.setCustomer(paymentReference.providerCustomerId());
            } else {
                return List.of();
            }
            return Invoice.list(builder.build()).getData().stream()
                    .map(invoice -> new InvoiceDetails(
                            invoice.getId(),
                            invoice.getNumber(),
                            toOffsetDateTime(invoice.getCreated()),
                            toOffsetDateTime(invoice.getDueDate()),
                            fromMinorUnits(invoice.getAmountPaid() == null ? invoice.getAmountDue() : invoice.getAmountPaid()),
                            invoice.getCurrency() == null ? null : invoice.getCurrency().toUpperCase(),
                            invoice.getStatus(),
                            invoice.getHostedInvoiceUrl()
                    ))
                    .toList();
        } catch (Exception exception) {
            throw new IllegalStateException("Stripe invoices could not be retrieved", exception);
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
        requireConfigured(properties.getSecretKey(), "Stripe secret key is not configured");
        Stripe.apiKey = properties.getSecretKey();
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
        if (metadata == null || !metadata.containsKey("clinicId") || !metadata.containsKey("planId") || !metadata.containsKey("billingCycle")) {
            throw new IllegalArgumentException("Stripe Checkout Session metadata is incomplete");
        }
        var stripeSubscription = retrieveSubscription(session.getSubscription());
        var paymentIntent = retrievePaymentIntent(session);
        var paymentMethod = retrievePaymentMethod(paymentIntent);
        var providerTransactionId = paymentIntent == null || isBlank(paymentIntent.getId()) ? session.getId() : paymentIntent.getId();
        return new CheckoutSessionCompletedPayment(
                session.getId(),
                paymentIntent == null ? null : paymentIntent.getId(),
                session.getPaymentStatus(),
                session.getStatus(),
                UUID.fromString(metadata.get("clinicId")),
                UUID.fromString(metadata.get("planId")),
                BillingCycle.valueOf(metadata.get("billingCycle")),
                fromMinorUnits(session.getAmountTotal()),
                session.getCurrency() == null ? null : session.getCurrency().toUpperCase(),
                new PaymentReference(
                        "STRIPE",
                        providerTransactionId,
                        session.getId(),
                        session.getCustomer(),
                        session.getSubscription(),
                        cardLast4(paymentMethod),
                        cardExpiresOn(paymentMethod)
                ),
                currentPeriodStart(stripeSubscription),
                currentPeriodEnd(stripeSubscription)
        );
    }

    private PaymentIntent retrievePaymentIntent(Session session) {
        if (session.getPaymentIntentObject() != null) {
            return session.getPaymentIntentObject();
        }
        if (isBlank(session.getPaymentIntent())) {
            return null;
        }
        try {
            return PaymentIntent.retrieve(session.getPaymentIntent());
        } catch (Exception exception) {
            throw new IllegalArgumentException("Stripe PaymentIntent could not be retrieved", exception);
        }
    }

    private PaymentMethod retrievePaymentMethod(PaymentIntent paymentIntent) {
        if (paymentIntent == null) return null;
        if (paymentIntent.getPaymentMethodObject() != null) {
            return paymentIntent.getPaymentMethodObject();
        }
        if (isBlank(paymentIntent.getPaymentMethod())) {
            return null;
        }
        try {
            return PaymentMethod.retrieve(paymentIntent.getPaymentMethod());
        } catch (Exception exception) {
            throw new IllegalArgumentException("Stripe PaymentMethod could not be retrieved", exception);
        }
    }

    private String cardLast4(PaymentMethod paymentMethod) {
        if (paymentMethod == null || paymentMethod.getCard() == null) return null;
        return paymentMethod.getCard().getLast4();
    }

    private String cardExpiresOn(PaymentMethod paymentMethod) {
        if (paymentMethod == null || paymentMethod.getCard() == null) return null;
        var expMonth = paymentMethod.getCard().getExpMonth();
        var expYear = paymentMethod.getCard().getExpYear();
        if (expMonth == null || expYear == null) return null;
        return "%02d/%d".formatted(expMonth, expYear);
    }

    private OffsetDateTime currentPeriodStart(com.stripe.model.Subscription subscription) {
        if (subscription == null || subscription.getItems() == null || subscription.getItems().getData().isEmpty()) {
            return OffsetDateTime.now();
        }
        return toOffsetDateTime(subscription.getItems().getData().getFirst().getCurrentPeriodStart());
    }

    private OffsetDateTime currentPeriodEnd(com.stripe.model.Subscription subscription) {
        if (subscription == null || subscription.getItems() == null || subscription.getItems().getData().isEmpty()) {
            return OffsetDateTime.now().plusMonths(1);
        }
        return toOffsetDateTime(subscription.getItems().getData().getFirst().getCurrentPeriodEnd());
    }

    private com.stripe.model.Subscription retrieveSubscription(String subscriptionId) {
        if (isBlank(subscriptionId)) return null;
        try {
            return com.stripe.model.Subscription.retrieve(subscriptionId);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Stripe Subscription could not be retrieved", exception);
        }
    }

    private String findPaymentMethodId(PaymentReference paymentReference) throws Exception {
        if (paymentReference == null) return null;
        if (!isBlank(paymentReference.providerSubscriptionId())) {
            var subscription = com.stripe.model.Subscription.retrieve(paymentReference.providerSubscriptionId());
            if (!isBlank(subscription.getDefaultPaymentMethod())) {
                return subscription.getDefaultPaymentMethod();
            }
        }
        if (!isBlank(paymentReference.providerCustomerId())) {
            var customer = Customer.retrieve(paymentReference.providerCustomerId());
            if (customer.getInvoiceSettings() != null && !isBlank(customer.getInvoiceSettings().getDefaultPaymentMethod())) {
                return customer.getInvoiceSettings().getDefaultPaymentMethod();
            }
        }
        return findPaymentMethodIdFromLatestInvoice(paymentReference);
    }

    private String findPaymentMethodIdFromLatestInvoice(PaymentReference paymentReference) throws Exception {
        var builder = InvoiceListParams.builder().setLimit(1L);
        if (!isBlank(paymentReference.providerSubscriptionId())) {
            builder.setSubscription(paymentReference.providerSubscriptionId());
        } else if (!isBlank(paymentReference.providerCustomerId())) {
            builder.setCustomer(paymentReference.providerCustomerId());
        } else {
            return null;
        }
        var invoices = Invoice.list(builder.build()).getData();
        if (invoices.isEmpty()) return null;
        var invoice = invoices.getFirst();
        if (!isBlank(invoice.getDefaultPaymentMethod())) {
            return invoice.getDefaultPaymentMethod();
        }
        if (invoice.getPayments() == null || invoice.getPayments().getData().isEmpty()) return null;
        var payment = invoice.getPayments().getData().getFirst().getPayment();
        if (payment == null || isBlank(payment.getPaymentIntent())) return null;
        var paymentIntent = PaymentIntent.retrieve(payment.getPaymentIntent());
        if (!isBlank(paymentIntent.getPaymentMethod())) {
            return paymentIntent.getPaymentMethod();
        }
        return null;
    }

    private long toMinorUnits(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Stripe amount is not configured");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Stripe amount must be greater than zero");
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private BigDecimal fromMinorUnits(Long amount) {
        if (amount == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(amount, 2);
    }

    private OffsetDateTime toOffsetDateTime(Long epochSeconds) {
        if (epochSeconds == null) return null;
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }

    private SessionCreateParams.LineItem.PriceData.Recurring.Interval toStripeInterval(BillingCycle billingCycle) {
        return billingCycle == BillingCycle.YEARLY
                ? SessionCreateParams.LineItem.PriceData.Recurring.Interval.YEAR
                : SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH;
    }

    private String urlOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String withCheckoutSessionId(String successUrl) {
        if (successUrl.contains("session_id=")) return successUrl;
        var separator = successUrl.contains("?") ? "&" : "?";
        return successUrl + separator + "session_id={CHECKOUT_SESSION_ID}";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String requireConfigured(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
        return value;
    }

    private String requireStripeTestSecretKey() {
        var secretKey = requireConfigured(properties.getSecretKey(), "Stripe secret key is not configured");
        if (secretKey.startsWith("pk_test_")) {
            throw new IllegalStateException("Stripe secret key must be a secret test key (sk_test_...), not a publishable key");
        }
        if (!secretKey.startsWith("sk_test_")) {
            throw new IllegalStateException("Stripe secret key must start with sk_test_");
        }
        return secretKey;
    }

    private void logStripeException(String message, StripeException exception) {
        LOGGER.error("{}: message='{}', code='{}', requestId='{}', statusCode={}",
                message,
                safeStripeMessage(exception),
                exception.getCode(),
                exception.getRequestId(),
                exception.getStatusCode(),
                exception);
    }

    private String safeStripeMessage(StripeException exception) {
        if (exception.getStripeError() != null && !isBlank(exception.getStripeError().getMessage())) {
            return exception.getStripeError().getMessage();
        }
        return isBlank(exception.getMessage()) ? "Stripe request failed" : exception.getMessage();
    }
}
