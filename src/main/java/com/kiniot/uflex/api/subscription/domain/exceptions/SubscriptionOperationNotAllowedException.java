package com.kiniot.uflex.api.subscription.domain.exceptions;

public class SubscriptionOperationNotAllowedException extends RuntimeException {
    public SubscriptionOperationNotAllowedException(String message) {
        super(message);
    }
}
