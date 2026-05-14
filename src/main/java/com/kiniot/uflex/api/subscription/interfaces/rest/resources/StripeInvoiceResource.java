package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record StripeInvoiceResource(
        String invoiceId,
        String number,
        OffsetDateTime issuedDate,
        OffsetDateTime dueDate,
        BigDecimal amount,
        String currency,
        String status,
        String hostedInvoiceUrl
) {
}
