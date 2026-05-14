package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "stripe")
public class StripePaymentProperties {
    private boolean enabled = false;
    private String secretKey = "";
    private String webhookSecret = "";
    private String successUrl = "http://localhost:4200/subscription?payment=success&session_id={CHECKOUT_SESSION_ID}";
    private String cancelUrl = "http://localhost:4200/subscription?payment=cancelled";
}
