package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.transform;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;
import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.model.CheckoutSessionCompletedPayment;
import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.model.InvoiceDetails;
import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.model.PaymentMethodDetails;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.checkout.Session;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@Service
public class StripePaymentMapper {

    public CheckoutSessionCompletedPayment toCompletedPayment(Session session,
                                                              PaymentIntent paymentIntent,
                                                              PaymentMethod paymentMethod,
                                                              com.stripe.model.Subscription stripeSubscription) {
        var metadata = session.getMetadata();
        validateCheckoutMetadata(metadata);

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

    public PaymentMethodDetails toPaymentMethodDetails(PaymentMethod paymentMethod) {
        var card = paymentMethod.getCard();
        return new PaymentMethodDetails(
                card.getBrand(),
                card.getLast4(),
                card.getExpMonth() == null ? null : card.getExpMonth().intValue(),
                card.getExpYear() == null ? null : card.getExpYear().intValue()
        );
    }

    public InvoiceDetails toInvoiceDetails(com.stripe.model.Invoice invoice) {
        return new InvoiceDetails(
                invoice.getId(),
                invoice.getNumber(),
                toOffsetDateTime(invoice.getCreated()),
                toOffsetDateTime(invoice.getDueDate()),
                fromMinorUnits(invoice.getAmountPaid() == null ? invoice.getAmountDue() : invoice.getAmountPaid()),
                invoice.getCurrency() == null ? null : invoice.getCurrency().toUpperCase(),
                invoice.getStatus(),
                invoice.getHostedInvoiceUrl()
        );
    }

    public long toMinorUnits(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Stripe amount is not configured");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Stripe amount must be greater than zero");
        }
        return amount.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
    }

    private void validateCheckoutMetadata(Map<String, String> metadata) {
        if (metadata == null || !metadata.containsKey("clinicId") || !metadata.containsKey("planId") || !metadata.containsKey("billingCycle")) {
            throw new IllegalArgumentException("Stripe Checkout Session metadata is incomplete");
        }
    }

    private BigDecimal fromMinorUnits(Long amount) {
        if (amount == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(amount, 2);
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

    private OffsetDateTime toOffsetDateTime(Long epochSeconds) {
        if (epochSeconds == null) return null;
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
