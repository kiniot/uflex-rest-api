package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.InvoiceId;

public record RegisterInvoicePaymentCommand(
        InvoiceId invoiceId,
        String providerTransactionId,
        boolean successful
) {
}
