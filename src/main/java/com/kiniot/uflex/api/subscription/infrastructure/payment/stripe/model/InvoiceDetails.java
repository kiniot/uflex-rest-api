package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.model;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.SubscriptionInvoiceDetails;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record InvoiceDetails(
        String invoiceId,
        String number,
        OffsetDateTime issuedDate,
        OffsetDateTime dueDate,
        BigDecimal amount,
        String currency,
        String status,
        String hostedInvoiceUrl
) implements SubscriptionInvoiceDetails {
}
