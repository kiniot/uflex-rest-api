package com.kiniot.uflex.api.subscription.interfaces.rest.controllers;

import com.kiniot.uflex.api.subscription.domain.services.StripeWebhookCommandService;
import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.configuration.StripePaymentProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/stripe")
public class StripeWebhookController {

    private final StripePaymentProperties stripePaymentProperties;
    private final StripeWebhookCommandService stripeWebhookCommandService;

    public StripeWebhookController(
            StripePaymentProperties stripePaymentProperties,
            StripeWebhookCommandService stripeWebhookCommandService
    ) {
        this.stripePaymentProperties = stripePaymentProperties;
        this.stripeWebhookCommandService = stripeWebhookCommandService;
    }

    @PostMapping
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String stripeSignature
    ) {
        if (stripePaymentProperties.getWebhookSecret() == null || stripePaymentProperties.getWebhookSecret().isBlank())
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        final Event event;
        try {
            event = Webhook.constructEvent(payload, stripeSignature, stripePaymentProperties.getWebhookSecret());
        } catch (SignatureVerificationException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);
        if (!(stripeObject instanceof Session session))
            return ResponseEntity.ok().build();
        switch (event.getType()) {
            case "checkout.session.completed" -> stripeWebhookCommandService.handleCheckoutCompleted(session.getId());
            case "checkout.session.expired" -> stripeWebhookCommandService.handleCheckoutExpired(session.getId());
            default -> {
            }
        }
        return ResponseEntity.ok().build();
    }
}
