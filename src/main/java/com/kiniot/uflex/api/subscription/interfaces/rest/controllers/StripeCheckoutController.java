package com.kiniot.uflex.api.subscription.interfaces.rest.controllers;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentGatewayPort;
import com.kiniot.uflex.api.subscription.domain.model.commands.CompleteCheckoutSessionPaymentCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionCommandService;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.ConfirmStripeCheckoutSessionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CreateStripeCheckoutSessionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.StripeCheckoutSessionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.SubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.StripeCheckoutSessionResourceFromResultAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.SubscriptionResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/api/v1/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subscriptions", description = "Available subscription endpoints")
public class StripeCheckoutController {
    private final SubscriptionCommandService subscriptionCommandService;
    private final PaymentGatewayPort paymentGatewayPort;
    private final ExternalIamService externalIamService;

    public StripeCheckoutController(SubscriptionCommandService subscriptionCommandService,
                                    PaymentGatewayPort paymentGatewayPort,
                                    ExternalIamService externalIamService) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.paymentGatewayPort = paymentGatewayPort;
        this.externalIamService = externalIamService;
    }

    @PostMapping("/checkout-session")
    public ResponseEntity<StripeCheckoutSessionResource> createCheckoutSession(@RequestBody CreateStripeCheckoutSessionResource resource) {
        try {
            var clinicId = externalIamService.fetchCurrentClinicId()
                    .orElseThrow(() -> new IllegalStateException("Authenticated user has no clinic assigned"));
            if (resource == null || resource.planId() == null) {
                throw new IllegalArgumentException("planId is required");
            }
            if (resource.billingCycle() == null || resource.billingCycle().isBlank()) {
                throw new IllegalArgumentException("billingCycle is required");
            }
            var command = new CreateSubscriptionCheckoutSessionCommand(
                    clinicId.id(),
                    resource.planId(),
                    BillingCycle.valueOf(resource.billingCycle()),
                    externalIamService.fetchCurrentUserId().orElse(null)
            );
            var checkoutSession = subscriptionCommandService.handle(command);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(StripeCheckoutSessionResourceFromResultAssembler.toResourceFromResult(checkoutSession));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw toHttpException(exception);
        }
    }

    @PostMapping("/checkout-session/confirm")
    public ResponseEntity<SubscriptionResource> confirmCheckoutSession(
            @RequestParam(value = "session_id", required = false) String sessionId,
            @RequestBody(required = false) ConfirmStripeCheckoutSessionResource resource) {
        try {
            var resolvedSessionId = sessionId != null && !sessionId.isBlank()
                    ? sessionId
                    : resource == null ? null : resource.sessionId();
            if (resolvedSessionId == null || resolvedSessionId.isBlank()) {
                throw new IllegalArgumentException("sessionId is required");
            }
            var clinicId = externalIamService.fetchCurrentClinicId()
                    .orElseThrow(() -> new IllegalStateException("Authenticated user has no clinic assigned"));
            var completedPayment = paymentGatewayPort.confirmCheckoutSession(resolvedSessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Stripe Checkout Session was not found"));
            if (!clinicId.id().equals(completedPayment.clinicId())) {
                throw new IllegalArgumentException("Stripe Checkout Session does not belong to the authenticated clinic");
            }
            return subscriptionCommandService.handle(new CompleteCheckoutSessionPaymentCommand(
                            completedPayment.clinicId(),
                            completedPayment.planId(),
                            completedPayment.billingCycle(),
                            completedPayment.paymentReference(),
                            completedPayment.currentPeriodStart(),
                            completedPayment.currentPeriodEnd()
                    ))
                    .map(SubscriptionResourceFromEntityAssembler::toResourceFromEntity)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw toHttpException(exception);
        }
    }

    private ResponseStatusException toHttpException(RuntimeException exception) {
        if ("Active plan not found".equals(exception.getMessage())) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
        if ("Clinic already has an active subscription".equals(exception.getMessage())) {
            return new ResponseStatusException(HttpStatus.CONFLICT, "Clinic already has an active subscription; plan changes through Checkout are not supported yet", exception);
        }
        if (exception.getMessage() != null && exception.getMessage().startsWith("Stripe Checkout Session could not be created")) {
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, exception.getMessage(), exception);
        }
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
}
