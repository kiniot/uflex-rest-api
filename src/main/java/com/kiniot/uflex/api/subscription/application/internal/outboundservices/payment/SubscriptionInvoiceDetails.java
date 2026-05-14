package com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface SubscriptionInvoiceDetails {
    String invoiceId();
    String number();
    OffsetDateTime issuedDate();
    OffsetDateTime dueDate();
    BigDecimal amount();
    String currency();
    String status();
    String hostedInvoiceUrl();
}
