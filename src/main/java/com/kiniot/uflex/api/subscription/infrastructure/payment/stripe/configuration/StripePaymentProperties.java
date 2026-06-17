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
    private boolean enabled = true;
    private String secretKey = "";
    private String webhookSecret = "";
    private String successUrl = "";
    private String cancelUrl = "";
}
