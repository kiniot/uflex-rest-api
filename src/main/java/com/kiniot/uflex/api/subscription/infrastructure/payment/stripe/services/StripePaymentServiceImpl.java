package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.services;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CompletedCheckoutPayment;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.SubscriptionInvoiceDetails;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.SubscriptionPaymentMethodDetails;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;
import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.StripePaymentService;
import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.model.CheckoutSessionCompletedPayment;
import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.model.CheckoutSessionResult;
import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.model.InvoiceDetails;
import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.model.PaymentMethodDetails;
import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.transform.StripePaymentMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.InvoiceListParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true")
public class StripePaymentServiceImpl implements StripePaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StripePaymentServiceImpl.class);

    private final StripeApiConfigurationService configurationService;
    private final StripePaymentMapper stripePaymentMapper;

    public StripePaymentServiceImpl(StripeApiConfigurationService configurationService,
                                    StripePaymentMapper stripePaymentMapper) {
        this.configurationService = configurationService;
        this.stripePaymentMapper = stripePaymentMapper;
    }

    @Override
    public CheckoutSessionResult createCheckoutSession(UUID clinicId,
                                                       UUID planId,
                                                       com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle billingCycle,
                                                       Money amount,
                                                       String successUrl,
                                                       String cancelUrl,
                                                       String planName,
                                                       String userId) {
        configurationService.useTestSecretKey();
        var resolvedSuccessUrl = configurationService.resolveSuccessUrl(successUrl);
        var resolvedCancelUrl = configurationService.resolveCancelUrl(cancelUrl);
        var currency = requireConfigured(amount.currency(), "Stripe currency is not configured").toLowerCase();
        var unitAmount = stripePaymentMapper.toMinorUnits(amount.amount());
        var resolvedPlanName = requireConfigured(planName, "Stripe product name is not configured");
        try {
            var params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(resolvedSuccessUrl)
                    .setCancelUrl(resolvedCancelUrl)
                    .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                            .putMetadata("tenantId", clinicId.toString())
                            .putMetadata("clinicId", clinicId.toString())
                            .putMetadata("planId", planId.toString())
                            .putMetadata("billingCycle", billingCycle.name())
                            .putMetadata("userId", nullToEmpty(userId))
                            .build())
                    .putMetadata("tenantId", clinicId.toString())
                    .putMetadata("clinicId", clinicId.toString())
                    .putMetadata("planId", planId.toString())
                    .putMetadata("billingCycle", billingCycle.name())
                    .putMetadata("userId", nullToEmpty(userId))
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(currency)
                                    .setUnitAmount(unitAmount)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(resolvedPlanName)
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
    public Optional<CompletedCheckoutPayment> confirmCheckoutSession(String sessionId) {
        configurationService.useSecretKey();
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Stripe Checkout Session id is required");
        }
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
    public Optional<SubscriptionPaymentMethodDetails> getPaymentMethod(PaymentReference paymentReference) {
        configurationService.useSecretKey();
        try {
            var paymentMethodId = findPaymentMethodId(paymentReference);
            if (isBlank(paymentMethodId)) return Optional.empty();
            var paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            if (paymentMethod.getCard() == null) return Optional.empty();
            return Optional.of(stripePaymentMapper.toPaymentMethodDetails(paymentMethod));
        } catch (Exception exception) {
            throw new IllegalStateException("Stripe payment method could not be retrieved", exception);
        }
    }

    @Override
    public List<SubscriptionInvoiceDetails> getInvoices(PaymentReference paymentReference) {
        configurationService.useSecretKey();
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
                    .map(invoice -> (SubscriptionInvoiceDetails) stripePaymentMapper.toInvoiceDetails(invoice))
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
        var webhookSecret = configurationService.webhookSecret();
        try {
            Webhook.constructEvent(payload, signature, webhookSecret);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public Optional<CompletedCheckoutPayment> handleCheckoutSessionCompleted(String payload, String signature) {
        var webhookSecret = configurationService.webhookSecret();
        configurationService.useSecretKey();
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Stripe webhook payload could not be parsed", exception);
        }
        if (!"checkout.session.completed".equals(event.getType())) {
            return Optional.empty();
        }
        return event.getDataObjectDeserializer().getObject()
                .filter(Session.class::isInstance)
                .map(Session.class::cast)
                .map(this::toCompletedPayment);
    }

    private CheckoutSessionCompletedPayment toCompletedPayment(Session session) {
        var stripeSubscription = retrieveSubscription(session.getSubscription());
        var paymentIntent = retrievePaymentIntent(session);
        var paymentMethod = retrievePaymentMethod(paymentIntent);
        return stripePaymentMapper.toCompletedPayment(session, paymentIntent, paymentMethod, stripeSubscription);
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
