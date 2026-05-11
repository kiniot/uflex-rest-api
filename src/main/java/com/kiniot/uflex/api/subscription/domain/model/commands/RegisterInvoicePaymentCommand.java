package com.kiniot.uflex.api.subscription.domain.model.commands;

import java.util.UUID;

public record RegisterInvoicePaymentCommand(
        UUID invoiceId,
        String providerTransactionId,
        boolean successful
) {
}
