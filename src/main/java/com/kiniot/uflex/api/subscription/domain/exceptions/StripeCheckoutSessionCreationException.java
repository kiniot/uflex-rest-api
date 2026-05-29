package com.kiniot.uflex.api.subscription.domain.exceptions;

public class StripeCheckoutSessionCreationException extends RuntimeException {
    public StripeCheckoutSessionCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
