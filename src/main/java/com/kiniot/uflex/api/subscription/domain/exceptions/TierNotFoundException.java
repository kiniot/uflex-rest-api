package com.kiniot.uflex.api.subscription.domain.exceptions;

public class TierNotFoundException extends RuntimeException {
    public TierNotFoundException(String tierId) {
        super("Tier not found: %s".formatted(tierId));
    }
}
