package com.kiniot.uflex.api.subscription.interfaces.rest.controllers;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.SubscriptionPaymentService;
import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetInvoiceHistoryQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetSubscriptionByClinicQuery;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionCommandService;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionQueryService;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CancelSubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CurrentSubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.InvoiceResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.StripeInvoiceResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.StripePaymentMethodResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.SubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.CancelSubscriptionCommandFromResourceAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.CurrentSubscriptionResourceFromEntityAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.InvoiceResourceFromEntityAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.SubscriptionResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subscriptions", description = "Available subscription endpoints")
public class SubscriptionController {
    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;
    private final ExternalIamService externalIamService;
    private final SubscriptionPaymentService paymentGatewayPort;

    public SubscriptionController(SubscriptionCommandService subscriptionCommandService,
                                  SubscriptionQueryService subscriptionQueryService,
                                  ExternalIamService externalIamService,
                                  SubscriptionPaymentService paymentGatewayPort) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionQueryService = subscriptionQueryService;
        this.externalIamService = externalIamService;
        this.paymentGatewayPort = paymentGatewayPort;
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentSubscriptionResource> getCurrentSubscription() {
        return currentSubscription()
                .map(CurrentSubscriptionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok().body(null));
    }

    @GetMapping("/current/payment-method")
    public ResponseEntity<StripePaymentMethodResource> getPaymentMethod() {
        try {
            var paymentMethod = currentSubscription()
                    .map(Subscription::getPaymentReference)
                    .flatMap(paymentGatewayPort::getPaymentMethod)
                    .map(method -> new StripePaymentMethodResource(method.brand(), method.last4(), method.expMonth(), method.expYear()))
                    .orElse(null);
            return ResponseEntity.ok(paymentMethod);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, exception.getMessage(), exception);
        }
    }

    @GetMapping("/current/invoices")
    public ResponseEntity<List<StripeInvoiceResource>> getCurrentSubscriptionInvoices() {
        try {
            var invoices = currentSubscription()
                    .map(Subscription::getPaymentReference)
                    .map(paymentGatewayPort::getInvoices)
                    .orElseGet(List::of)
                    .stream()
                    .map(invoice -> new StripeInvoiceResource(
                            invoice.invoiceId(),
                            invoice.number(),
                            invoice.issuedDate(),
                            invoice.dueDate(),
                            invoice.amount(),
                            invoice.currency(),
                            invoice.status(),
                            invoice.hostedInvoiceUrl()
                    ))
                    .toList();
            return ResponseEntity.ok(invoices);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, exception.getMessage(), exception);
        }
    }

    @PostMapping("/current/cancel")
    public ResponseEntity<SubscriptionResource> cancelCurrentSubscription(@RequestBody(required = false) CancelSubscriptionResource resource) {
        try {
            var subscription = currentSubscription()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current subscription not found"));
            var command = CancelSubscriptionCommandFromResourceAssembler.toCommandFromResource(subscription.getId().id(), resource);
            return subscriptionCommandService.handle(command)
                    .map(SubscriptionResourceFromEntityAssembler::toResourceFromEntity)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    private Optional<Subscription> currentSubscription() {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authenticated user has no clinic assigned"));
        return subscriptionQueryService.handle(new GetSubscriptionByClinicQuery(clinicId));
    }
}
