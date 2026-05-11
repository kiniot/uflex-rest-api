package com.kiniot.uflex.api.subscription.interfaces.rest.controllers;

import com.kiniot.uflex.api.subscription.domain.services.SubscriptionCommandService;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CreateStripeCheckoutSessionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.StripeCheckoutSessionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.CreateSubscriptionCheckoutSessionCommandFromResourceAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.StripeCheckoutSessionResourceFromResultAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/api/v1/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subscriptions", description = "Available subscription endpoints")
public class StripeCheckoutController {
    private final SubscriptionCommandService subscriptionCommandService;

    public StripeCheckoutController(SubscriptionCommandService subscriptionCommandService) {
        this.subscriptionCommandService = subscriptionCommandService;
    }

    @PostMapping("/checkout-session")
    public ResponseEntity<StripeCheckoutSessionResource> createCheckoutSession(@RequestBody CreateStripeCheckoutSessionResource resource) {
        try {
            var command = CreateSubscriptionCheckoutSessionCommandFromResourceAssembler.toCommandFromResource(resource);
            var checkoutSession = subscriptionCommandService.handle(command);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(StripeCheckoutSessionResourceFromResultAssembler.toResourceFromResult(checkoutSession));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }
}
