package com.kiniot.uflex.api.subscription.domain.exceptions;

import java.math.BigDecimal;

public class InvalidSubscriptionAmountFormatException extends RuntimeException {
    public InvalidSubscriptionAmountFormatException(BigDecimal amount) {
        super("Subscription amount must be sent as a numeric decimal with exactly 2 fractional digits, for example 90.00. Received: %s"
                .formatted(amount.toPlainString()));
    }
}
