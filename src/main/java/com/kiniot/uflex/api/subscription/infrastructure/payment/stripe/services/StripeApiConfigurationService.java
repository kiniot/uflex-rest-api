package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.services;

import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.configuration.StripePaymentProperties;
import com.stripe.Stripe;
import org.springframework.stereotype.Service;

@Service
public class StripeApiConfigurationService {

    private final StripePaymentProperties properties;

    public StripeApiConfigurationService(StripePaymentProperties properties) {
        this.properties = properties;
    }

    public String useSecretKey() {
        var secretKey = requireConfigured(properties.getSecretKey(), "Stripe secret key is not configured");
        Stripe.apiKey = secretKey;
        return secretKey;
    }

    public String useTestSecretKey() {
        var secretKey = useSecretKey();
        if (secretKey.startsWith("pk_test_")) {
            throw new IllegalStateException("Stripe secret key must be a secret test key (sk_test_...), not a publishable key");
        }
        if (!secretKey.startsWith("sk_test_")) {
            throw new IllegalStateException("Stripe secret key must start with sk_test_");
        }
        return secretKey;
    }

    public String webhookSecret() {
        return requireConfigured(properties.getWebhookSecret(), "Stripe webhook secret is not configured");
    }

    public String resolveSuccessUrl(String requestedSuccessUrl) {
        return withCheckoutSessionId(urlOrDefault(requestedSuccessUrl, properties.getSuccessUrl()));
    }

    public String resolveCancelUrl(String requestedCancelUrl) {
        return urlOrDefault(requestedCancelUrl, properties.getCancelUrl());
    }

    private String requireConfigured(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
        return value;
    }

    private String urlOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String withCheckoutSessionId(String successUrl) {
        if (successUrl.contains("session_id=")) return successUrl;
        var separator = successUrl.contains("?") ? "&" : "?";
        return successUrl + separator + "session_id={CHECKOUT_SESSION_ID}";
    }
}
