package com.kiniot.uflex.api.subscription.domain.exceptions;

public class SubscriptionPriceMismatchException extends RuntimeException {
    public SubscriptionPriceMismatchException() {
        super("Contracted price must match the tier catalog price");
    }
}
