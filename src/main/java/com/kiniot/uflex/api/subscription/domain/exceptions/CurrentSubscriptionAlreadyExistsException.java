package com.kiniot.uflex.api.subscription.domain.exceptions;

public class CurrentSubscriptionAlreadyExistsException extends RuntimeException {
    public CurrentSubscriptionAlreadyExistsException() {
        super("Current clinic already has a current subscription");
    }
}
