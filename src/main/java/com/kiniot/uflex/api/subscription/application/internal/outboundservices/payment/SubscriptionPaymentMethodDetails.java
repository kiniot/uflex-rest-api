package com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment;

public interface SubscriptionPaymentMethodDetails {
    String brand();
    String last4();
    Integer expMonth();
    Integer expYear();
}
