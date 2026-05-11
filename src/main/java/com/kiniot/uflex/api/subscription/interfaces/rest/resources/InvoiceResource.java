package com.kiniot.uflex.api.subscription.interfaces.rest.resources;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.InvoiceStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InvoiceResource(
        UUID id,
        UUID subscriptionId,
        BigDecimal amount,
        String currency,
        OffsetDateTime issuedAt,
        OffsetDateTime dueAt,
        OffsetDateTime paidAt,
        InvoiceStatus status,
        String providerTransactionId
) {
}
