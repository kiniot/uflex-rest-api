package com.kiniot.uflex.api.subscription.interfaces.rest.controllers;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentGatewayPort;
import com.kiniot.uflex.api.subscription.domain.model.commands.CompleteCheckoutSessionPaymentCommand;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionCommandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/api/v1/webhooks/stripe", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Stripe Webhooks", description = "Stripe Checkout sandbox webhook endpoints")
public class StripeWebhookController {
    private final PaymentGatewayPort paymentGatewayPort;
    private final SubscriptionCommandService subscriptionCommandService;

    public StripeWebhookController(PaymentGatewayPort paymentGatewayPort,
                                   SubscriptionCommandService subscriptionCommandService) {
        this.paymentGatewayPort = paymentGatewayPort;
        this.subscriptionCommandService = subscriptionCommandService;
    }

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(@RequestBody String payload, @RequestHeader HttpHeaders headers) {
        var signature = headers.getFirst("Stripe-Signature");
        if (!paymentGatewayPort.verifyWebhookSignature(payload, signature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Stripe webhook signature");
        }
        paymentGatewayPort.handleCheckoutSessionCompleted(payload, signature)
                .ifPresent(payment -> subscriptionCommandService.handle(new CompleteCheckoutSessionPaymentCommand(
                        payment.clinicId(),
                        payment.planId(),
                        payment.billingCycle(),
                        payment.paymentReference(),
                        payment.currentPeriodStart(),
                        payment.currentPeriodEnd()
                )));
        return ResponseEntity.noContent().build();
    }
}
